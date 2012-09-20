package com.psyonik.upnp
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import org.xml.sax.helpers.XMLReaderFactory
import org.xml.sax.InputSource
import scala.tools.nsc.io.File
import java.net.URI
import java.io.BufferedReader
import java.io.FileReader
import org.specs2.specification.Scope

/** When a Upnp command is send to the router an xml file is generated on the router and sent back.
  * Sample xml response files are used here to test NameValueHandler.
  * The test files can be generated from a router using the code in "GenFilesForNameValueHandlerSpec"
  */
@RunWith(classOf[JUnitRunner])
class NameValueHandlerUnitSpec extends Specification {
  val testFilePath = "src/test/resources/com/psyonik/upnp/namevaluehandlerspec/"

  trait system extends Scope {
    def testFileName: GatewayDevice.Commands.Value;
    val xmlFile = new java.io.File(testFilePath + testFileName + ".xml")
    xmlFile.exists() mustEqual true

    val reader = new BufferedReader(new FileReader(xmlFile));

    val nameValue = new scala.collection.mutable.HashMap[String, String];
    val parser = XMLReaderFactory.createXMLReader();

    parser.setContentHandler(new NameValueHandler(nameValue));

    val input = new InputSource(reader);

    parser.parse(input); //loads nameValue through sideEffects

    reader.close();
  }

  "NameValueHandler" should {
    import GatewayDevice.Commands._
    "properly handle parsing the test GetStatusInfo.xml file" in new system {
      def testFileName = GetStatusInfo

      val t = nameValue.get("NewConnectionStatus").
        filter(_.equalsIgnoreCase("Connected")).
        map(_ ⇒ true)
      t.getOrElse(false) mustEqual true

      nameValue("NewLastConnectionError") mustEqual "ERROR_NONE"
      nameValue("NewUptime") mustEqual "1312938"

    }
    "properly handle parsing the test GetExternalIPAddress.xml file" in new system {
      def testFileName = GetExternalIPAddress

      nameValue("NewExternalIPAddress") mustEqual "74.77.125.7"

    }

  }
}