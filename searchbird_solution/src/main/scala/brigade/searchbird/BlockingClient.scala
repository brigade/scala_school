package brigade.searchbird

import brigade.searchbird.thrift.SearchbirdService
import com.twitter.finagle.Thrift
import com.twitter.util.Await

class BlockingClient {
  val service = Thrift.client.newIface[SearchbirdService.FutureIface]("localhost:9999")

  def get(key: String) = Await.result(service.get(key))
  def put(key: String, value: String) = Await.result(service.put(key, value))
  def search(query: String) = Await.result(service.search(query))
}
