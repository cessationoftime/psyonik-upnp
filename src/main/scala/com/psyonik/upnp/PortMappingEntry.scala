package com.psyonik.upnp

class PortMappingEntry {
  /** The internal port
    */
  var internalPort: Option[Int] = None;
  /** The external port of the mapping (the one on the GatewayDevice)
    */
  var externalPort: Option[Int] = None;
  /** The remote host this mapping is associated with
    */
  var remoteHost: Option[String] = None;
  /** The internal host this mapping is associated with
    */
  var internalClient: Option[String] = None;
  /** The protocol associated with this mapping (i.e. <tt>TCP</tt> or
    * <tt>UDP</tt>)
    */
  var protocol: Option[String] = None;
  /** A flag that tells whether the mapping is enabled or not
    * (<tt>"1"</tt> for enabled, <tt>"0"</tt> for disabled)
    */
  var enabled: Option[String] = None;
  /** A human readable description of the port mapping (used for display
    * purposes)
    */
  var portMappingDescription: Option[String] = None;
}