package com.psyonik.upnp
import scala.collection.immutable.HashMap
import org.xml.sax.SAXException
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

class GatewayDeviceHandler(device: GatewayDevice) extends DefaultHandler {

  private var currentElement: Option[String] = None;
  private var level: Int = 0;
  private var state: Short = 0;

  /** Receive notification of the start of an element.
    *
    * Caches the element as {@link #currentElement}, and keeps track of some
    * basic state information.
    *
    * @param uri The Namespace URI, or the empty string if the
    * element has no Namespace URI or if Namespace
    * processing is not being performed.
    * @param localName The local name (without prefix), or the
    * empty string if Namespace processing is not being
    * performed.
    * @param qName The qualified name (with prefix), or the
    * empty string if qualified names are not available.
    * @param attributes The attributes attached to the element.  If
    * there are no attributes, it shall be an empty
    * Attributes object.
    * @exception org.xml.sax.SAXException Any SAX exception, possibly
    * wrapping another exception.
    * @see org.xml.sax.ContentHandler#startElement
    */
  @throws(classOf[SAXException])
  override def startElement(uri: String, localName: String, qName: String,
    attributes: Attributes) = {
    currentElement = Some(localName);
    level += 1;
    if (state < 1 && "serviceList".compareTo(currentElement.get) == 0) {
      state = 1;
    }
  }

  /** Receive notification of the end of an element.
    *
    * Used to update state information.
    *
    * <p>By default, do nothing.  Application writers may override this
    * method in a subclass to take specific actions at the end of
    * each element (such as finalising a tree node or writing
    * output to a file).</p>
    *
    * @param uri The Namespace URI, or the empty string if the
    * element has no Namespace URI or if Namespace
    * processing is not being performed.
    * @param localName The local name (without prefix), or the
    * empty string if Namespace processing is not being
    * performed.
    * @param qName The qualified name (with prefix), or the
    * empty string if qualified names are not available.
    * @exception org.xml.sax.SAXException Any SAX exception, possibly
    *   wrapping another exception.
    * @see org.xml.sax.ContentHandler#endElement
    */
  @throws(classOf[SAXException])
  override def endElement(uri: String, localName: String, qName: String) = {
    currentElement = Some("")
    level -= 1;
    if (localName.compareTo("service") == 0) {
      if (device.serviceTypeCIF.isDefined &&
        device.serviceTypeCIF.get.compareTo("urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1") == 0)
        state = 2;
      if (device.serviceType.isDefined &&
        device.serviceType.get.compareTo("urn:schemas-upnp-org:service:WANIPConnection:1") == 0)
        state = 3;
    }
  }

  /** Receive notification of character data inside an element.
    *
    * It is used to read the values of the relevant fields of the device being
    * configured.
    *
    * @param ch The characters.
    * @param start The start position in the character array.
    * @param length The number of characters to use from the
    *       character array.
    * @exception org.xml.sax.SAXException Any SAX exception, possibly
    *    wrapping another exception.
    * @see org.xml.sax.ContentHandler#characters
    */
  @throws(classOf[SAXException])
  override def characters(ch: Array[Char], start: Int, length: Int) = {
    val s: Option[String] = Some(new String(ch, start, length));
    if (currentElement.get.compareTo("URLBase") == 0)
      device.urlBase = s;
    else if (state <= 1) {
      if (state == 0) {
        if ("friendlyName".compareTo(currentElement.get) == 0)
          device.friendlyName = s;
        else if ("manufacturer".compareTo(currentElement.get) == 0)
          device.manufacturer = s;
        else if ("modelDescription".compareTo(currentElement.get) == 0)
          device.modelDescription = s;
        else if ("presentationURL".compareTo(currentElement.get) == 0)
          device.presentationURL = s;
        else if ("modelNumber".compareTo(currentElement.get) == 0)
          device.modelNumber = s;
        else if ("modelName".compareTo(currentElement.get) == 0)
          device.modelName = s;
      }
      if (currentElement.get.compareTo("serviceType") == 0)
        device.serviceTypeCIF = s;
      else if (currentElement.get.compareTo("controlURL") == 0)
        device.controlURLCIF = s;
      else if (currentElement.get.compareTo("eventSubURL") == 0)
        device.eventSubURLCIF = s;
      else if (currentElement.get.compareTo("SCPDURL") == 0)
        device.SCPDURLCIF = s;
      else if (currentElement.get.compareTo("deviceType") == 0)
        device.deviceTypeCIF = s;
    } else if (state == 2) {
      if (currentElement.get.compareTo("serviceType") == 0)
        device.serviceType = s;
      else if (currentElement.get.compareTo("controlURL") == 0)
        device.controlURL = s;
      else if (currentElement.get.compareTo("eventSubURL") == 0)
        device.eventSubURL = s;
      else if (currentElement.get.compareTo("SCPDURL") == 0)
        device.SCPDURL = s;
      else if (currentElement.get.compareTo("deviceType") == 0)
        device.deviceType = s;

    }
  }

  //Well shit. I can't do this because some of the key names are shared between states. :/
  /*
  val hash = HashMap(
      "friendlyName" -> (device.friendlyName_=_, 0),
      "manufacturer" -> (device.manufacturer_=_, 0),
      "modelDescription" -> (device.modelDescription_=_, 0),
      "presentationURL" -> (device.presentationURL_=_, 0),
      "modelNumber" -> (device.modelNumber_=_, 0),
      "modelName" -> (device.modelName_=_, 0),
      "serviceType" -> (device.serviceType_=_, 1),
      "controlURL" -> (device.controlURL_=_, 1),
      "eventSubURL" -> (device.eventSubURL_=_, 1),
      "SCPDURL" -> (device.SCPDURL_=_, 1),
  )
  */
}