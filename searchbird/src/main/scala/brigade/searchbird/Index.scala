package brigade.searchbird

import brigade.searchbird.thrift.{SearchbirdService, SearchbirdException}
import com.twitter.finagle.Thrift

import com.twitter.util._
import com.twitter.logging.Logger

import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConverters._


trait Index {
  def get(key: String): Future[String]
  def put(key: String, value: String): Future[Unit]
  def search(key: String): Future[Seq[String]]
}

class ResidentIndex extends Index {
  val log = Logger.get(getClass)

  val forward = new ConcurrentHashMap[String, String].asScala
  val reverse = new ConcurrentHashMap[String, Set[String]].asScala

  def get(key: String) = {
    forward.get(key) match {
      case None =>
        log.debug("get %s: miss", key)
        Future.exception(new SearchbirdException("No such key"))
      case Some(value) =>
        log.debug("get %s: hit", key)
        Future(value)
    }
  }

  def put(key: String, value: String) = {
    log.debug("put %s", key)
    
    forward(key) = value

    // admit only one updater.
    synchronized {
      (Set() ++ value.split(" ")) foreach { token =>
        val current = reverse.get(token) getOrElse Set()
        reverse(token) = current + key
      }
    }

    Future.Unit
  }

  def search(query: String) = Future.value {
    val tokens = query.split(" ")
    val hits = tokens map { token => reverse.getOrElse(token, Set()) }
    val intersected = hits reduceLeftOption { _ & _ } getOrElse Set()
    intersected.toList
  }
}

class CompositeIndex(indices: Seq[Index]) extends Index {
  require(indices.nonEmpty)

  def get(key: String) = try {
    println("GET", key)
    val queries = indices.map { idx =>
      idx.get(key) map { r => Some(r) } handle { case e => None }
    }

    Future.collect(queries) flatMap { results =>
      println("got results", results.mkString(","))
      results.find { _.isDefined } map { _.get } match {
        case Some(v) => Future.value(v)
        case None => Future.exception(new SearchbirdException("No such key"))
      }
    }
  } catch {
    case NonFatal(e) =>
      println("got exc", e)
      throw e
  }

  def put(key: String, value: String) =
    Future.exception(new SearchbirdException("put() not supported by CompositeIndex"))

  def search(query: String) = {
    val queries = indices.map { _.search(query) rescue { case _=> Future.value(Nil) } }
    Future.collect(queries) map { results => (Set() ++ results.flatten).toList }
  }
}

class RemoteIndex(host: String) extends Index {
  val client = Thrift.client.newIface[SearchbirdService.FutureIface](host)

  def get(key: String) = client.get(key)

  def put(key: String, value: String) = client.put(key, value)

  def search(query: String) = client.search(query)
}

