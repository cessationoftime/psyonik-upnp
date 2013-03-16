package com.psyonik.upnp
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import org.specs2.mock.Mockito
import java.net.InetAddress
import scala.collection.JavaConversions._
import scala.collection.immutable.HashMap
import scala.collection.parallel.immutable.ParMap

class GatewayDiscoverSpec extends Specification with Mockito {
  args(sequential = true)
  trait system extends Scope {
    //Thread.sleep(1000)
    val gateways = GatewayDiscover();
  }

  "GatewayDiscover" should {

    "return the same number of gateways." in new system {
      println("gateway size:" + gateways.devices.size)
      
	  var counter = 0;
      gateways foreach {
        case (key, gw) =>
          {
            counter = counter + 1;
          
            println("Listing gateway details of device #" + counter +
              "\n\tFriendly name: " + gw.RootDevice.friendlyName +
              "\n\tPresentation URL: " + gw.RootDevice.presentationURL +
              "\n\tModel name: " + gw.RootDevice.modelName +
              "\n\tModel number: " + gw.RootDevice.modelNumber +
              "\n\tLocal interface address: " + gw.localAddress.getHostAddress() + "\n" +
			  "\n\tExternal interface address: " + gw.externalIPAddress + "\n");
          }
      }

    }

     "return a valid gateway in Scala." in new system {
      gateways.getValidGateway() mustNotEqual None;
    }

  }

  "getLocalInetAddresses" should {
    "get the IP addresses of the local system, not the router" in new system {
      val addresses = GatewayDiscover.getLocalInetAddresses(true, false, false)
	  addresses foreach { addr =>
		println(addr);
	  }
    }
  }
}