package brigade.searchbird

import com.twitter.finagle.Thrift
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import java.net.InetSocketAddress

object Server extends TwitterServer {

  def main() {
    val impl = new SearchbirdServiceImpl
    val server = Thrift.server
      .serveIface(new InetSocketAddress(9999), impl)
    Await.ready(server)
  }
}

