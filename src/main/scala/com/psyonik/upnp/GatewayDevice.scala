package com.psyonik.upnp
import java.io.IOException
import org.xml.sax.SAXException
import java.net.URL
import java.net.HttpURLConnection
import org.xml.sax.helpers.XMLReaderFactory
import org.xml.sax.InputSource
import org.xml.sax.XMLReader
import java.net.URLConnection
import org.xml.sax.helpers.DefaultHandler
import java.net.InetAddress
import scala.collection.JavaConversions._
import java.io.File
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.Reader
import java.io.InputStreamReader
import scala.xml.Attribute
import scala.xml.Unparsed
import scala.xml.Group
import scala.xml.Elem
import scala.xml.TopScope
import scala.xml.NodeSeq
import scala.xml.Node

/** A <tt>GatewayDevice</tt> is a class that abstracts UPnP-compliant gateways
  * <p/>
  * It holds all the information that comes back as UPnP responses, and
  * provides methods to issue UPnP commands to a gateway.
  *
  * @author casta (original)
  * @author ses-jeff (scala)
  *
  */
class GatewayDevice(var controlURL: Option[String], var serviceType: Option[String]) {
  import GatewayDevice.Commands._
  def this() = this(None, None)

  var st: Option[String] = None;
  var location: Option[String] = None;
  //var serviceType: Option[String] = None;
  var serviceTypeCIF: Option[String] = None;
  var urlBase: Option[String] = None;
  //var controlURL: Option[String] = None;
  var controlURLCIF: Option[String] = None;
  var eventSubURL: Option[String] = None;
  var eventSubURLCIF: Option[String] = None;
  var SCPDURL: Option[String] = None;
  var SCPDURLCIF: Option[String] = None;
  var deviceType: Option[String] = None;
  var deviceTypeCIF: Option[String] = None;

  /** The friendly (human readable) name associated with this device
    */
  var friendlyName: Option[String] = None;

  /** The device manufacturer name
    */
  var manufacturer: Option[String] = None;

  /** The model description as a string
    */
  var modelDescription: Option[String] = None;

  /** The URL that can be used to access the IGD interface
    */
  var presentationURL: Option[String] = None;

  /** The address used to reach this machine from the GatewayDevice
    */
  var localAddress: Option[InetAddress] = None;

  /** The model number (used by the manufacturer to identify the product)
    */
  var modelNumber: Option[String] = None;

  /** The model name
    */
  var modelName: Option[String] = None;

  /** Retrieves the properties and description of the GatewayDevice.
    * <p/>
    * Connects to the device's {@link #location} and parses the response
    * using a {@link GatewayDeviceHandler} to populate the fields of this
    * class
    *
    * @throws SAXException if an error occurs while parsing the request
    * @throws IOException  on communication errors
    * @see org.bitlet.weupnpscala.GatewayDeviceHandler
    */

  @throws(classOf[SAXException])
  @throws(classOf[IOException])
  def loadDescription() = {
    //TODO: Fix the use of options here.
    //Using all of them in a match statement won't work.
    val urlConn: URLConnection = new URL(location.get).openConnection();
    urlConn.setReadTimeout(GatewayDevice.HTTP_RECEIVE_TIMEOUT);

    val parser: XMLReader = XMLReaderFactory.createXMLReader();
    parser.setContentHandler(new GatewayDeviceHandler(this));
    val inputStream = urlConn.getInputStream();
    //parser.parse(new InputSource(urlConn.getInputStream()));
    parser.parse(new InputSource(inputStream));

    //This is why using all of them in a match statement won't work.
    /* fix urls */
    var ipConDescURL: String = new String();
    if (urlBase.isDefined && urlBase.get.trim().length() > 0) {
      ipConDescURL = urlBase.get;
    } else {
      ipConDescURL = location.get;
    }

    val lastSlashIndex: Int = ipConDescURL.indexOf('/', 7);
    if (lastSlashIndex > 0) {
      ipConDescURL = ipConDescURL.substring(0, lastSlashIndex);
    }

    SCPDURL = Some(copyOrCatUrl(ipConDescURL, SCPDURL.get));
    controlURL = Some(copyOrCatUrl(ipConDescURL, controlURL.get));
    controlURLCIF = Some(copyOrCatUrl(ipConDescURL, controlURLCIF.get));
    presentationURL = Some(copyOrCatUrl(ipConDescURL, presentationURL.get));
  }

  /** Retrieves the connection status of this device
    *
    * @return true if connected, false otherwise
    * @throws IOException
    * @throws SAXException
    * @see #simpleUPnPcommand(java.lang.String, java.lang.String,
    * java.lang.String, java.util.Map)
    */
  @throws(classOf[IOException])
  @throws(classOf[SAXException])
  lazy val isConnected: Boolean = {
    (controlURL, serviceType) match {
      case (Some(controlURLValue), Some(serviceTypeValue)) => {
        val nameValue: Map[String, String] = GatewayDevice.simpleUPnPcommand(controlURLValue,
          serviceTypeValue, GetStatusInfo);

        //Needs to be optimized as an option
        val t = nameValue.get("NewConnectionStatus").
          filter(_.equalsIgnoreCase("Connected")).
          map(_ => true)

        t.getOrElse(false)
      }
      case _ => false;
    }

  }

  /** Retrieves the external IP address associated with this device
    * <p/>
    * The external address is the address that can be used to connect to the
    * GatewayDevice from the external network
    *
    * @return the external IP
    * @throws IOException
    * @throws SAXException
    * @see #simpleUPnPcommand(java.lang.String, java.lang.String,
    * java.lang.String, java.util.Map)
    */
  @throws(classOf[IOException])
  @throws(classOf[SAXException])
  lazy val externalIPAddress: Option[String] = {
    //TODO: Print out if controlUrl or ServiceType are None.
    (controlURL, serviceType) match {
      case (Some(controlURLValue), Some(serviceTypeValue)) => {
        val nameValue: Map[String, String] = GatewayDevice.simpleUPnPcommand(controlURLValue,
          serviceTypeValue, GetExternalIPAddress);
        nameValue.get("NewExternalIPAddress");
      }
      case (_) => {
        None;
      }
    }
  }

  /** Adds a new port mapping to the GatewayDevices using the supplied
    * parameters.
    *
    * @param externalPort   the external associated with the new mapping
    * @param internalPort   the internal port associated with the new mapping
    * @param internalClient the internal client associated with the new mapping
    * @param protocol       the protocol associated with the new mapping
    * @param description    the mapping description
    * @return true if the mapping was succesfully added, false otherwise
    * @throws IOException
    * @throws SAXException
    * @see #simpleUPnPcommand(java.lang.String, java.lang.String,
    * java.lang.String, java.util.Map)
    * @see PortMappingEntry
    */
  @throws(classOf[IOException])
  @throws(classOf[SAXException])
  def addPortMapping(externalPort: Int, internalPort: Int,
    internalClient: String, protocol: String, description: String, leaseDuration: Int = 0): Boolean = {
    //TODO: Print out if controlUrl or ServiceType are None.
    (controlURL, serviceType) match {
      case (Some(controlURLValue), Some(serviceTypeValue)) =>
        val args: Map[String, String] = Map("NewRemoteHost" -> "", //wildcard, any remote host matches
          "NewExternalPort" -> Integer.toString(externalPort),
          "NewProtocol" -> protocol,
          "NewInternalPort" -> Integer.toString(internalPort),
          "NewInternalClient" -> internalClient,
          "NewEnabled" -> "1",
          "NewPortMappingDescription" -> description,
          "NewLeaseDuration" -> Integer.toString(leaseDuration));

        val nameValue: Map[String, String] = GatewayDevice.simpleUPnPcommand(controlURLValue,
          serviceTypeValue, AddPortMapping, args);
		  
        nameValue.get("errorCode").isEmpty
      case _ => false;
    }
  }

  private def portQueryToPortMappingEntry(nameValue: Map[String, String]) : Option[PortMappingEntry] = {
   if (nameValue.isEmpty || nameValue.contains("errorCode"))
		return None;
  	Some(PortMappingEntry(
	nameValue.get("NewInternalPort") map (_.toInt),
	nameValue.get("NewExternalPort") map (_.toInt),
	nameValue.get("NewRemoteHost"),
	nameValue.get("NewInternalClient"),
    nameValue.get("NewProtocol"),
    nameValue.get("NewEnabled"),
    nameValue.get("NewPortMappingDescription")));
  }
  
  /** Queries the GatewayDevice to retrieve a specific port mapping entry,
    * corresponding to specified criteria, if present.
    * <p/>
    * Retrieves the <tt>PortMappingEntry</tt> associated with
    * <tt>externalPort</tt> and <tt>protocol</tt>, if present.
    *
    * @param externalPort     the external port
    * @param protocol         the protocol (TCP or UDP)
    * @return Some(PortMappingEntry) if a valid mapping is found, else None
    * @throws IOException
    * @throws SAXException
    * @todo consider refactoring this method to make it consistent with
    * Java practices (return the port mapping)
    * @see #simpleUPnPcommand(java.lang.String, java.lang.String,
    * java.lang.String, java.util.Map)
    * @see PortMappingEntry
    */
  @throws(classOf[IOException])
  @throws(classOf[SAXException])
  def getSpecificPortMappingEntry(externalPort: Int,
    protocol: String): Option[PortMappingEntry] = {
	
    (controlURL, serviceType) match {
      case (Some(controlURLValue), Some(serviceTypeValue)) =>
        val args: Map[String, String] = Map(
          "NewRemoteHost" -> "", // wildcard, any remote host matches
          "NewExternalPort" -> Integer.toString(externalPort),
          "NewProtocol" -> protocol);

        val nameValue: Map[String, String] = GatewayDevice.simpleUPnPcommand(controlURLValue,
          serviceTypeValue, GetSpecificPortMappingEntry, args);

        if (!nameValue.contains("NewInternalClient") || !nameValue.contains("NewInternalPort")) {
           None;
		  } else {
		  portQueryToPortMappingEntry(args ++ nameValue)
		}
      case _ => None;
      
    }
  }

  /** Returns a specific port mapping entry, depending on a the supplied index.
    *
    * @param index            the index of the desired port mapping
    * @return Some(PortMappingEntry) if a valid mapping is found, else None
    * @throws IOException
    * @throws SAXException
    * @todo consider refactoring this method to make it consistent with
    * Java practices (return the port mapping)
    * @see #simpleUPnPcommand(java.lang.String, java.lang.String,
    * java.lang.String, java.util.Map)
    * @see PortMappingEntry
    */
  @throws(classOf[IOException])
  @throws(classOf[SAXException])
  def getGenericPortMappingEntry(index: Int): Option[PortMappingEntry] = {

    //TODO: There appears to be a lot of reuse from getSpecificPortMappingEntry.
    //Combine the two methods to a degree?

    (controlURL, serviceType) match {
      case (Some(controlURLValue), Some(serviceTypeValue)) =>
        var args: Map[String, String] = Map("NewPortMappingIndex" -> Integer.toString(index));

        val nameValue: Map[String, String] = GatewayDevice.simpleUPnPcommand(controlURLValue,
          serviceTypeValue, GetGenericPortMappingEntry, args);

		portQueryToPortMappingEntry(nameValue)

      case _ => None;
      
    }
  }

  /** Retrieves the number of port mappings that are registered on the
    * GatewayDevice.
    *
    * @return the number of port mappings
    * @throws IOException
    * @throws SAXException
    */
  @throws(classOf[IOException])
  @throws(classOf[SAXException])
  def getPortMappingNumberOfEntries(): Int = {
    (controlURL, serviceType) match {
      case (Some(controlURLValue), Some(serviceTypeValue)) => 
        val nameValue: Map[String, String] = GatewayDevice.simpleUPnPcommand(controlURLValue,
          serviceTypeValue, GetPortMappingNumberOfEntries);

          //This originally used Integer.valueOf. Not sure if .toInt will mess it up.
            nameValue.get("NewPortMappingNumberOfEntries").getOrElse("0").toInt;
      case _ =>  0      
    }
  }

  /** Deletes the port mapping associated to <tt>externalPort</tt> and
    * <tt>protocol</tt>
    *
    * @param externalPort the external port
    * @param protocol     the protocol
    * @return true if removal was successful
    * @throws IOException
    * @throws SAXException
    */
  @throws(classOf[IOException])
  @throws(classOf[SAXException])
  def deletePortMapping(externalPort: Int, protocol: String): Boolean = {
    //TODO: You know, this doesn't actually confirm that it worked...
    //It just kind of returns true if it tried to do it.
    (controlURL, serviceType) match {
      case (Some(controlURLValue), Some(serviceTypeValue)) =>
          val args: Map[String, String] = Map(
            "NewRemoteHost" -> "",
            "NewExternalPort" -> Integer.toString(externalPort),
            "NewProtocol" -> protocol);
          val nameValue: Map[String, String] = GatewayDevice.simpleUPnPcommand(controlURLValue,
            serviceTypeValue, DeletePortMapping, args);
          true;
      case _ => false;
    }
  }

  // private methods

  private def copyOrCatUrl(dst: String, src: String): String = {
    var dstValue = dst;
    var srcValue = src;
    if (srcValue != null) {
      if (srcValue.startsWith("http://")) {
        dstValue = srcValue;
      } else {
        if (!srcValue.startsWith("/")) {
          dstValue += "/";
        }
        dstValue += srcValue;
      }
    }
    return dstValue;
  }
}
object GatewayDevice {
  object Commands extends Enumeration {
    // type Commands = Value
    val GetStatusInfo, GetExternalIPAddress, AddPortMapping, GetSpecificPortMappingEntry, GetGenericPortMappingEntry, GetPortMappingNumberOfEntries, DeletePortMapping = Value
  }
  val HTTP_RECEIVE_TIMEOUT = 7000;

  private def makeSoap(action: Commands.Value, args: Map[String, String], service: String) = {
    def buildArgs: collection.immutable.Seq[Node] = {
      args.map {
        case (key, value) => {

          scala.xml.Elem(null, key, scala.xml.Null, scala.xml.TopScope, scala.xml.Text(value))
          // soapBody.append("<" + key + ">" + value + "</" + key + ">");
        }
        // }
      }.toSeq
    }

    val header = Unparsed("""<?xml version="1.0"?>""")
    val soapBody = <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
                     <SOAP-ENV:Body>
                       { Elem("m", action.toString, Attribute("xmlns", "m", service, scala.xml.Null), TopScope, buildArgs: _*) }
                     </SOAP-ENV:Body>
                   </SOAP-ENV:Envelope>;
    Group(List(header, soapBody)).toString();
  }

  private def issueUPnpCommand(url: String, service: String, action: Commands.Value, args: Map[String, String], doParse: InputSource => Unit): Unit = {
    var soapAction: String = "\"" + service + "#" + action + "\""
    //   val soapBody = new StringBuilder();
    //<m:{action} xmlns:m={ service }/>
    //    soapBody.append("" +
    val soapXmlBytes = makeSoap(action, args, service).getBytes();

    val postUrl = new URL(url);
    val conn = postUrl.openConnection().asInstanceOf[HttpURLConnection];

    conn.setRequestMethod("POST");
    conn.setReadTimeout(HTTP_RECEIVE_TIMEOUT);
    conn.setDoOutput(true);
    conn.setRequestProperty("Content-Type", "text/xml");
    conn.setRequestProperty("SOAPAction", soapAction);
    conn.setRequestProperty("Connection", "Close");

    conn.setRequestProperty("Content-Length", String.valueOf(soapXmlBytes.length));

    conn.getOutputStream().write(soapXmlBytes);

    val stream = if (conn.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR)
      conn.getErrorStream
    else
      conn.getInputStream

    doParse(new InputSource(stream));
    conn.disconnect();
  }

  /** Issues UPnP commands to a GatewayDevice that can be reached at the
    * specified <tt>url</tt>
    * <p/>
    * The command is identified by a <tt>service</tt> and an <tt>action</tt>
    * and can receive arguments
    *
    * @param url     the url to use to contact the device
    * @param service the service to invoke
    * @param action  the specific action to perform
    * @param args    the command arguments
    * @return the response to the performed command, as a name-value map.
    * In case errors occur, the returned map will be <i>empty.</i>
    * @throws IOException  on communication errors
    * @throws SAXException if errors occur while parsing the response
    */
  @throws(classOf[IOException])
  @throws(classOf[SAXException])
  def simpleUPnPcommand(url: String, service: String, action: Commands.Value, args: Map[String, String]): Map[String, String] = {
    val nameValue = new scala.collection.mutable.HashMap[String, String];

    //handles parsing the result of the UPnP command into a HashMap
    val parseIt: InputSource => Unit = { inputSource =>
      val parser = XMLReaderFactory.createXMLReader();
      parser.setContentHandler(new NameValueHandler(nameValue));
      try {
        parser.parse(inputSource);
      } catch {
        case e: SAXException => {}
        case e => throw new RuntimeException(e);
      }
    }

    issueUPnpCommand(url, service, action, args, parseIt)

    return nameValue.toMap;
  }

  /** Issues UPnP commands to a GatewayDevice that can be reached at the
    * specified <tt>url</tt>
    * <p/>
    * The command is identified by a <tt>service</tt> and an <tt>action</tt>
    * and can receive arguments
    *
    * @param url     the url to use to contact the device
    * @param service the service to invoke
    * @param action  the specific action to perform
    * @return the response to the performed command, as a name-value map.
    * In case errors occur, the returned map will be <i>empty.</i>
    * @throws IOException  on communication errors
    * @throws SAXException if errors occur while parsing the response
    */
  @throws(classOf[IOException])
  @throws(classOf[SAXException])
  def simpleUPnPcommand(url: String, service: String, action: Commands.Value): Map[String, String] = {
    simpleUPnPcommand(url, service, action, Map.empty)
  }

  /** Issues a simpleUPnPcommand, but instead of parsing the result into a HashMap it places the result in an XML file.  The XML can then be used for testing, etc.
    */
  def simpleUPnPcommand_createFile(url: String, service: String, action: Commands.Value, args: Map[String, String], filename: String): Unit = {

    //handles parsing the result of the UPnP command into a file
    val parseIt: InputSource => Unit = { inputSource =>
      val body = new InputStreamReader(inputSource.getByteStream());

      var c: Int = 0;
      def red = { c = body.read(); c }
      try {
        val f = new FileWriter(filename);
        while (red > -1) {
          f.write(c);
        }
        f.flush()
        f.close();
      } catch {
        case e: Exception => {
          System.err.println("Exception: " + e.getMessage());
          e.printStackTrace();
        }
      }
    }

    issueUPnpCommand(url, service, action, args, parseIt)
  }

}
