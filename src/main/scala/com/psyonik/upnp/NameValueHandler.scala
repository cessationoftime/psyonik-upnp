package com.psyonik.upnp
import org.xml.sax.helpers.DefaultHandler
import org.xml.sax.SAXException
import org.xml.sax.Attributes
import scala.collection.mutable.Map

class NameValueHandler(var nameValue: Map[String, String]) extends DefaultHandler {

  var currentElement: String = null;

  /** Receive notification of the start of an element.
    *
    * Caches the element as {@link #currentElement}, so that it will be stored
    * as a map key when the corresponding value will be read.
    *
    * @param uri (Unused, null it) The Namespace URI, or the empty string if the
    * element has no Namespace URI or if Namespace
    * processing is not being performed.
    * @param localName The local name (without prefix), or the
    * empty string if Namespace processing is not being
    * performed.
    * @param qName (Unused, null it) The qualified name (with prefix), or the
    * empty string if qualified names are not available.
    * @param attributes (Unused, null it) The attributes attached to the element.  If
    * there are no attributes, it shall be an empty
    * Attributes object.
    * @exception org.xml.sax.SAXException Any SAX exception, possibly
    * wrapping another exception.
    * @see org.xml.sax.ContentHandler#startElement
    */
  @throws(classOf[SAXException])
  override def startElement(uri: String, localName: String, qName: String,
    attributes: Attributes) = {
    currentElement = localName;
  }

  def startElement(localName: String) {
    this.startElement(null, localName, null, null);
  }

  /** Receive notification of the end of an element.
    *
    * It is used to reset currentElement when the XML node is closed.
    * Note: this works only when the data we are interested in does not contain
    * child nodes.
    *
    * Based on a patch provided by christophercyll and attached to issue #4:
    * http://code.google.com/p/weupnp/issues/detail?id=4
    *
    * @param uri (Unused, null it) The Namespace URI, or the empty string if the
    * element has no Namespace URI or if Namespace
    * processing is not being performed.
    * @param localName (Unused, null it) The local name (without prefix), or the
    * empty string if Namespace processing is not being
    * performed.
    * @param qName (Unused, null it) The qualified name (with prefix), or the
    * empty string if qualified names are not available.
    * @throws SAXException Any SAX exception, possibly
    * wrapping another exception.
    */
  @throws(classOf[SAXException])
  override def endElement(uri: String, localName: String, qName: String) {
    currentElement = null;
  }

  def endElement() {
    this.endElement(null, null, null);
  }

  /** Receive notification of character data inside an element.
    *
    * Stores the characters as value, using {@link #currentElement} as a key
    *
    * @param ch The characters.
    * @param start The start position in the character array.
    * @param length The number of characters to use from the
    * character array.
    * @exception org.xml.sax.SAXException Any SAX exception, possibly
    * wrapping another exception.
    * @see org.xml.sax.ContentHandler#characters
    */

  @throws(classOf[SAXException])
  override def characters(ch: Array[Char], start: Int, length: Int) = {
    if (currentElement != null) {
      val value = new String(ch, start, length);
      val old = nameValue.get(currentElement);
      //nameValue = if (old == None) {
      if (old == None) {
        //nameValue + (currentElement -> value);
        nameValue.put(currentElement, value);
      } else {
        //nameValue + (currentElement -> (old + value));
        nameValue.put(currentElement, old + value);
      }
    }
  }
}
