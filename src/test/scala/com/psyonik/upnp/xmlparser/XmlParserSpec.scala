package com.psyonik.upnp.xmlparser
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import org.specs2.mock.Mockito
import java.net.InetAddress
import scala.collection.JavaConversions._
import scala.collection.immutable.HashMap
import scala.collection.parallel.immutable.ParMap
import scala.io.Source
import scala.xml.Elem
import scala.xml.NodeSeq
import com.psyonik.upnp._

class XmlParserSpec extends Specification with Mockito {
  args(sequential = true)

  val testFilePath = "src/test/resources/com/psyonik/upnp/xmlparser/"

  trait system extends Scope {
    val internetGatewayDevice = new java.io.File(testFilePath + "InternetGatewayDevice.xml")
    val getExternalIPAddress = new java.io.File(testFilePath + "GetExternalIPAddress.xml")
    val getStatusInfo = new java.io.File(testFilePath + "GetStatusInfo.xml")

    internetGatewayDevice.exists() mustEqual true
    getExternalIPAddress.exists() mustEqual true
    getStatusInfo.exists() mustEqual true

    // val bufferedSource = Source.fromFile(xmlFile)

  }

  "XmlParser" should {

    "be able to parse the xml" in new system {
      val internetGatewayDeviceXml = scala.xml.XML.loadFile(internetGatewayDevice)

      (internetGatewayDeviceXml \ "URLBase").textOption mustEqual None
      (internetGatewayDeviceXml \ "device" \ "manufacturer").textOption mustEqual Some("Cisco")

    }

  }

  "NameValueHandler" should {
    "properly handle parsing the test GetStatusInfo.xml file" in new system {
      val getStatusInfoXml = scala.xml.XML.loadFile(getStatusInfo)
      
    val nameValue =  (getStatusInfoXml \\ "_").map(x => (x.label, x.text) ).toMap
      

            val t = nameValue.get("NewConnectionStatus").
              filter(_.equalsIgnoreCase("Connected")).
              map(_ ⇒ true)
            t.getOrElse(false) mustEqual true
      
            nameValue("NewLastConnectionError") mustEqual "ERROR_NONE"
            nameValue("NewUptime") mustEqual "1312938"
    }
    "properly handle parsing the test GetExternalIPAddress.xml file" in new system {
        val getExternalIPAddressXml = scala.xml.XML.loadFile(getExternalIPAddress)

        val nameValue =  (getExternalIPAddressXml \\ "_").map(x => (x.label, x.text) ).toMap

         nameValue("NewExternalIPAddress") mustEqual "74.77.125.7"
      
    }
  }

}