package brigade.searchbird

import brigade.searchbird.thrift.SearchbirdService
import com.twitter.util.Future


class SearchbirdServiceImpl(index: Index) extends SearchbirdService.FutureIface {

  override def get(key: String): Future[String] = {
    index.get(key)
  }

  override def put(key: String, value: String): Future[Unit] = {
    index.put(key, value)
  }

  override def search(query: String): Future[Seq[String]] = {
    index.search(query)
  }
}
