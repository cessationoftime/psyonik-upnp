package com.psyonik.upnp.genTestFiles
import org.specs2.mutable.Specification
import com.psyonik.upnp.GatewayDevice
import org.specs2.specification.Scope

/** This Specification is not a real set of tests, but rather it is a utility to generate XML resources to use while running the NameValueHandlerSpec
  */
class GenFilesForNameValueHandlerSpec extends Specification {
  skipAll // causes this Specification to be ignored
  import GatewayDevice.Commands._
  val controlURLValue = "http://192.168.10.254:4444/wipconn"; //setting for cvanvranken home router (Trendnet TEW-633GR)
  val serviceTypeValue = "urn:schemas-upnp-org:service:WANIPConnection:1";

  def makeFile(commandName: GatewayDevice.Commands.Value) = GatewayDevice.simpleUPnPcommand_createFile(controlURLValue,
    serviceTypeValue, commandName, Map.empty, "src/test/resources/com/psyonik/upnp/namevaluehandlerspec/" + commandName + ".xml");

  "GenFilesForNameValueHandler" should {
    "Generate XML for the GetExternalIPAddress,GetStatusInfo command" in new Scope {
      List(GetExternalIPAddress, GetStatusInfo) foreach (makeFile(_));
    }
  }

}