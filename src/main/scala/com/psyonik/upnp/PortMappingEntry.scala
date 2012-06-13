package com.psyonik.upnp

/**
@param internalPort The internal port
@param externalPort The external port of the mapping (the one on the GatewayDevice)
@param remoteHost The remote host this mapping is associated with
@param internalClient The internal host this mapping is associated with
@param protocol The protocol associated with this mapping (i.e. <tt>TCP</tt> or <tt>UDP</tt>)
@param enabled A flag that tells whether the mapping is enabled or not (<tt>"1"</tt> for enabled, <tt>"0"</tt> for disabled)
@param portMappingDescription A human readable description of the port mapping (used for display purposes)
**/
case class PortMappingEntry private[upnp] (
internalPort: Option[Int], externalPort: Option[Int], 
remoteHost: Option[String], internalClient: Option[String],
protocol: Option[String], enabled: Option[String], portMappingDescription: Option[String])
