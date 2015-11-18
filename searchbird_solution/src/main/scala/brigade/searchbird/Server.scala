package brigade.searchbird

import com.twitter.finagle.Thrift
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import java.net.InetSocketAddress

object Server extends TwitterServer {

  def main() {
    val index = new ResidentIndex
    val impl = new SearchbirdServiceImpl(index)
    val server = Thrift.server
      .serveIface(new InetSocketAddress(9999), impl)
    Await.ready(server)
  }
}

