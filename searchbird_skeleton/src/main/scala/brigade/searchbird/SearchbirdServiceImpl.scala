package brigade.searchbird

import brigade.searchbird.thrift.SearchbirdService
import com.twitter.util.Future


class SearchbirdServiceImpl extends SearchbirdService.FutureIface {

  override def get(key: String): Future[String] = ???

  override def put(key: String, value: String): Future[Unit] = ???

  override def search(query: String): Future[Seq[String]] = ???
}
