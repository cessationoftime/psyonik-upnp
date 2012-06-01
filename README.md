psyonik-upnp
==========

Scala implementation of uPnP library.
This library was derived from the Java library [weupnp](http://code.google.com/p/weupnp/)

Example:

```scala
import com.psyonik.upnp._

import java.lang.Thread

import scala.collection.JavaConversions.mapAsScalaMap
import scala.util.control.Breaks.break
import scala.util.control.Breaks.breakable
object Main extends App {
  val SAMPLE_PORT = 6991;
  val WAIT_TIME = 10;
  val LISTALLMAPPINGS = false;
  var counter = 0;

  //The printlns here are supposed to be AddLogline(string), but I can't find them.
  println("Starting weupnp");
 // val gatewayDiscover = new GatewayDiscover();

  println("Looking for Gateway Devices...");

  val gateways = GatewayDiscover();

  if (gateways.isEmpty) {
    println("No gateways found");
    println("Stopping weupnp");
    sys.exit();
  }
  println(gateways.size + "gateway(s) found");
  gateways foreach {
    case (key, gw) =>
      {
        counter = counter + 1;
        println("Listing gateway details of device #" + counter +
          "\n\tFriendly name: " + gw.friendlyName +
          "\n\tPresentation URL: " + gw.presentationURL +
          "\n\tModel name: " + gw.modelName +
          "\n\tModel number: " + gw.modelNumber +
          "\n\tLocal interface address: " + gw.localAddress.get.getHostAddress() + "\n");
      }
  }

  val activeGWOption = gateways.getValidGateway();
  
  activeGWOption match {
  case Some(gw) =>println("Using gateway: " + gw.friendlyName);
  case None =>  println("No active gateway device found");
	println("exiting");
	sys.exit();
  }
  val activeGW = activeGWOption.get;

  val portMapCount = activeGW.getPortMappingNumberOfEntries();
  println("GetPortMappingNumberOfEntries=" + (if (portMapCount != 0) portMapCount.toString else "(unsupported)"));

  val portMapping0 = new PortMappingEntry();
  if (LISTALLMAPPINGS) {
    var pmCount = 0;
    breakable {
      do {
        if (activeGW.getGenericPortMappingEntry(pmCount, portMapping0))
          println("Portmapping #" + pmCount + " successfully retrieved (" + portMapping0.portMappingDescription + ":" + portMapping0.externalPort + ")");
        else {
          println("Portmapping #" + pmCount + " retrival failed");
          break;
        }
        pmCount += 1;
      } while (portMapping0 != null);
    }
  } else {
    if (activeGW.getGenericPortMappingEntry(0, portMapping0))
      println("Portmapping #0 successfully retrieved (" + portMapping0.portMappingDescription + ":" + portMapping0.externalPort + ")");
    else
      println("Portmapping #0 retrival failed");
  }

  val localAddress = activeGW.localAddress;
  println("Using local address: " + localAddress.get.getHostAddress());
  val externalIPAddress = activeGW.externalIPAddress;
  println("External address: " + externalIPAddress);

  println("Querying device to see if a port mapping already exists for port: " + SAMPLE_PORT);
  val portMapping = new PortMappingEntry();

  if (activeGW.getSpecificPortMappingEntry(SAMPLE_PORT, "TCP", portMapping)) {
    println("Port " + SAMPLE_PORT + " is already mapped. Aborting test.");
    sys.exit();
  } else {
    println("Mapping free. Sending port mapping request for port " + SAMPLE_PORT);

    if (activeGW.addPortMapping(SAMPLE_PORT, SAMPLE_PORT, localAddress.get.getHostAddress(), "TCP", "test")) {
      println("Mapping Successful. Waiting " + WAIT_TIME + " seconds before removing mapping...");
      Thread.sleep(1009 * WAIT_TIME);

      if (activeGW.deletePortMapping(SAMPLE_PORT, "TCP") == true)
        println("Port mapping removed, test SUCCESSFUL");
      else
        println("Port mapping removal FAILED");
    }
  }
  println("Stopping weupnp");
}
```