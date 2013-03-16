package com.psyonik.upnp
import org.specs2.mutable.Specification

class GatewayDeviceUnitSpec extends Specification {
  //val controlURLValue = "http://192.168.1.1:4444/wipconn"; //old Trendnet router
  val controlURLValue = "http://192.168.1.1:1780/control?WANIPConnection"; //Cisco Linksys E2000
  val serviceTypeValue = "urn:schemas-upnp-org:service:WANIPConnection:1";
  "GatewayDevice" should {
    import GatewayDevice.Commands._
    " issue GetStatusInfo command to see if it isConnected" in {

      val nameValue: Map[String, String] = GatewayDevice.simpleUPnPcommand(controlURLValue, serviceTypeValue, GetStatusInfo);

      nameValue.size must beGreaterThan(0)

      nameValue("NewConnectionStatus") must beEqualTo("Connected").ignoreCase
      nameValue("NewLastConnectionError") must beEqualTo("ERROR_NONE").ignoreCase
      nameValue("NewUptime") must beMatching("""\d+""")

    }
    "issue GetExternalIPAddress command" in {
      val nameValue: Map[String, String] = GatewayDevice.simpleUPnPcommand(controlURLValue,
        serviceTypeValue, GetExternalIPAddress);

      nameValue.size must beGreaterThan(0)

      nameValue("NewExternalIPAddress") must beMatching("""\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}""")

      //      val t = nameValue.get("NewConnectionStatus").
      //        filter(_.equalsIgnoreCase("Connected")).
      //        map(_ ⇒ true)
      //      t.getOrElse(false) mustEqual true
    }
  }

}