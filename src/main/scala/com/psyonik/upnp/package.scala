package com.psyonik
import java.net.InetAddress

package object upnp {
import GatewayDiscover._
   //implicitly convert Map[InetAddress, GatewayDevice] to GatewayDeviceMap
    implicit def map2GatewayDeviceMap(o: Map[InetAddress, GatewayDevice]): GatewayDeviceMap = new GatewayDeviceMap(o)
	implicit def gatewayDeviceMap2Map(o: GatewayDeviceMap): Map[InetAddress, GatewayDevice] = o.devices
}