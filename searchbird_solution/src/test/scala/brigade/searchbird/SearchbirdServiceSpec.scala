package brigade.searchbird

import brigade.searchbird.thrift.SearchbirdException
import com.twitter.util.{Await, Future}
import org.scalatest.{Matchers, WordSpec}

class SearchbirdServiceSpec extends WordSpec with Matchers {

  "SearchbirdService" should {
    // TODO: Please implement your own tests.

    val index = new ResidentIndex
    val searchbird = new SearchbirdServiceImpl(index)

    "set a key, get a key" in {
      Await.result(searchbird.put("name", "bluebird"))
      Await.result(searchbird.get("name")) should equal("bluebird")
      a[SearchbirdException] shouldBe thrownBy {
        Await.result(searchbird.get("what?"))
      }
    }
  }
}
