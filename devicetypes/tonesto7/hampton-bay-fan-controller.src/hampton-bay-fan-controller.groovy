/**
 *  King Of Fans Zigbee Fan Controller
 *
 *  To be used with Ceiling Fan Remote Controller Model MR101Z receiver by Chungear Industrial Co. Ltd
 *  at Home Depot Gardinier 52" Ceiling Fan, Universal Ceiling Fan/Light Premier Remote Model #99432
 *
 *  Copyright 2017 Ranga Pedamallu, Stephan Hackett, Dale Coffing
 *
 *  Contributing Authors:
	   tonesto7; rewrote to simplify and make it actually work ;)
	   Ranga Pedamallu; initial release and zigbee parsing mastermind!
	   Stephan Hackett; new composite (child) device type genius!
	   Dale Coffing; icons, multiAttribute fan, code maintenance flunky
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */

import groovy.transform.Field
@Field String version = "v0.3.0"
@Field String iconsUrl = "https://cdn.rawgit.com/dcoffing/KOF-CeilingFan/master/resources/images/"
@Field String childFanVer = "v0.3.0"
@Field String childLightVer = "v0.3.0"

metadata {
	definition (name: "Hampton Bay Fan Controller", namespace: "tonesto7", author: "Stephan Hackett, Ranga Pedamallu, Dale Coffing") {
		capability "Actuator"
		capability "Configuration"
		capability "Refresh"
		capability "Switch"
		capability "Light"
		capability "Switch Level"
		capability "Sensor"
		capability "Polling"
		//capability "Health Check"

		command "lightOn"
		command "lightOff"
		command "lightLevel"
		command "setFanSpeed"
		command "adjFanSpeed"

		command "lowSpeed"
		command "medSpeed"
		command "medHighSpeed"
		command "highSpeed"
		command "breeze"

		attribute "fanMode", "string"
		attribute "curFanMode", "string"
		attribute "fanSpeedDesc", "string"
		attribute "lightLevel", "number"	//stores brightness level
		attribute "lightStatus", "number"	//stores on/off
		attribute "fanSpeed", "number"
		attribute "lastFanMode", "string"	//used to restore previous fanmode
		attribute "LchildVer", "string"		//stores light child version
		attribute "FchildVer", "string"		//stores fan child version
		attribute "LchildCurr", "string"	//stores color of version check
		attribute "FchildCurr", "string"	//stores color of version check

		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0202", outClusters: "0003, 0019", model: "HDC52EastwindFan"
	}

	preferences {
		page(name: "childToRebuild", title: "This does not display on DTH preference page")
			section("section") {
				input(name: "refreshChildren", type: "bool", title: "Delete & Recreate all child devices?\n\nTypically used after modifying the parent device name " +
				"above to give all child devices the new name.\n\nPLEASE NOTE: Child Devices must be removed from any smartApps BEFORE attempting this " +
				"process or 'An unexpected error' occurs attempting to delete the child's.")
	   }
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "switch", type: "generic", width: 6, height: 4) {
			tileAttribute ("fanMode", key: "PRIMARY_CONTROL") {
				attributeState "04", label:"HIGH", action:"off", icon:iconsUrl+"fan4h.png", backgroundColor:"#79b821", nextState: "turningOff"
				attributeState "03", label:"MED-HI", action:"off", icon:iconsUrl+"fan3h.png", backgroundColor:"#79b821", nextState: "turningOff"
				attributeState "02", label:"MED", action:"off", icon:iconsUrl+"fan2h.png", backgroundColor:"#79b821", nextState: "turningOff"
				attributeState "01", label:"LOW", action:"off", icon:iconsUrl+"fan1h.png", backgroundColor:"#79b821", nextState: "turningOff"
				attributeState "06", label:"BREEZE", action:"off", icon:iconsUrl+"breeze4h_blk.png", backgroundColor:"#008B64", nextState: "turningBreezeOff"
				attributeState "00", label:"FAN OFF", action:"on", icon:iconsUrl+"fan00h_grey.png", backgroundColor:"#ffffff", nextState: "turningOn"
				attributeState "turningOn", action:"on", label:"TURNING ON", icon:iconsUrl+"fan0h.png", backgroundColor:"#2179b8", nextState: "turningOn"
				attributeState "turningOff", action:"off", label:"TURNING OFF", icon:iconsUrl+"fan0h_grey.png", backgroundColor:"#2179b8", nextState: "turningOff"
				attributeState "turningBreezeOff", action:"off", label:"TURNING OFF", icon:iconsUrl+"breeze4h_teal.png", backgroundColor:"#2179b8", nextState: "turningOff"
			}
			tileAttribute ("device.lightLevel", key: "SECONDARY_CONTROL") {
				attributeState "lightLevel", label:'Light: ${currentValue}%', icon:"st.switches.light.on"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", label:"${currentValue}%", action:"switch level.setLevel", icon:iconsUrl+"fan0h.png"
			}
		}
		standardTile("refresh", "refresh", decoration: "flat", width: 2, height: 3) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		valueTile("version", "version", width:4, height:1) {
			state "version", label:"Ceiling Fan Parent\n"+ version
		}
		standardTile("fanMode1", "fanMode", decoration: "flat", width: 2, height: 2) {
			state "00", label:"LOW", action: "lowSpeed", icon: iconsUrl+"fan1j_off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "01", label:"LOW", action: "off", icon: iconsUrl+"fan1j_on.png", backgroundColor: "#79b821", nextState: "turningOff"
			state "02", label:"LOW", action: "lowSpeed", icon: iconsUrl+"fan1j_off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "03", label:"LOW", action: "lowSpeed", icon: iconsUrl+"fan1j_off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "04", label:"LOW", action: "lowSpeed", icon: iconsUrl+"fan1j_off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "06", label:"LOW", action: "lowSpeed", icon: iconsUrl+"fan1j_off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "turningOn", label:"ADJUSTING", icon: iconsUrl+"fan1j_on.png", backgroundColor: "#2179b8", nextState: "turningOn"
			state "turningOff", label:"ADJUSTING", icon: iconsUrl+"fan1j_off.png", backgroundColor: "#2179b8", nextState: "turningOff"
		}
		standardTile("fanMode2", "fanMode", decoration: "flat", width: 2, height: 2) {
			state "00", label:"MED", action: "medSpeed", icon: iconsUrl+"fan2j_off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "01", label:"MED", action: "medSpeed", icon: iconsUrl+"fan2j_off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "02", label:"MED", action: "off", icon: iconsUrl+"fan2j_on.png", backgroundColor: "#79b821", nextState: "turningOff"
			state "03", label:"MED", action: "medSpeed", icon: iconsUrl+"fan2j_off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "04", label:"MED", action: "medSpeed", icon: iconsUrl+"fan2j_off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "06", label:"MED", action: "medSpeed", icon: iconsUrl+"fan2j_off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "turningOn", label:"ADJUSTING", action: "on", icon: iconsUrl+"fan2j_on.png", backgroundColor: "#2179b8", nextState: "turningOn"
			state "turningOff", label:"ADJUSTING", action: "off", icon: iconsUrl+"fan2j_off.png", backgroundColor: "#2179b8", nextState: "turningOff"
		}
		standardTile("fanMode3", "fanMode", decoration: "flat", width: 2, height: 2) {
			state "00", label:"MED-HI", action: "medHighSpeed", icon: iconsUrl+"fan3j_off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "01", label:"MED-HI", action: "medHighSpeed", icon: iconsUrl+"fan3j_off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "02", label:"MED-HI", action: "medHighSpeed", icon: iconsUrl+"fan3j_off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "03", label:"MED-HI", action: "off", icon: iconsUrl+"fan3j_on.png", backgroundColor: "#79b821", nextState: "turningOff"
			state "04", label:"MED-HI", action: "medHighSpeed", icon: iconsUrl+"fan3j_off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "06", label:"MED-HI", action: "medHighSpeed", icon: iconsUrl+"fan3j_off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "turningOn", label:"ADJUSTING", action: "on", icon: iconsUrl+"fan3j_on.png", backgroundColor: "#2179b8", nextState: "turningOn"
			state "turningOff", label:"ADJUSTING", action: "off", icon: iconsUrl+"fan3j_off.png", backgroundColor: "#2179b8", nextState: "turningOff"
		}
		standardTile("fanMode4", "fanMode", decoration: "flat", width: 2, height: 2) {
			state "00", label:"HIGH", action: "highSpeed", icon: iconsUrl+"fan4j_off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "01", label:"HIGH", action: "highSpeed", icon: iconsUrl+"fan4j_off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "02", label:"HIGH", action: "highSpeed", icon: iconsUrl+"fan4j_off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "03", label:"HIGH", action: "highSpeed", icon: iconsUrl+"fan4j_off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "04", label:"HIGH", action: "off", icon: iconsUrl+"fan4j_on.png", backgroundColor: "#79b821", nextState: "turningOff"
			state "06", label:"HIGH", action: "highSpeed", icon: iconsUrl+"fan4j_off.png", backgroundColor: "#ffffff", nextState: "turningOn"
			state "turningOn", label:"ADJUSTING", action: "on", icon: iconsUrl+"fan4j_on.png", backgroundColor: "#2179b8", nextState: "turningOn"
			state "turningOff", label:"ADJUSTING", action: "off", icon: iconsUrl+"fan4j_off.png", backgroundColor: "#2179b8", nextState: "turningOff"
		}
		standardTile("fanMode6", "fanMode", decoration: "flat", width: 2, height: 2) {
			state "00", label:"BREEZE", action: "breeze", icon: iconsUrl+"breeze4h_off.png", backgroundColor: "#ffffff", nextState: "turningBreezeOn"
			state "01", label:"BREEZE", action: "breeze", icon: iconsUrl+"breeze4h_off.png", backgroundColor: "#ffffff", nextState: "turningBreezeOn"
			state "02", label:"BREEZE", action: "breeze", icon: iconsUrl+"breeze4h_off.png", backgroundColor: "#ffffff", nextState: "turningBreezeOn"
			state "03", label:"BREEZE", action: "breeze", icon: iconsUrl+"breeze4h_off.png", backgroundColor: "#ffffff", nextState: "turningBreezeOn"
			state "04", label:"BREEZE", action: "breeze", icon: iconsUrl+"breeze4h_off.png", backgroundColor: "#ffffff", nextState: "turningBreezeOn"
			state "06", label:"BREEZE", action: "off", icon: iconsUrl+"breeze4h_teal.png", backgroundColor: "#79b821", nextState: "turningBreezeOff"
			state "turningBreezeOn", label:"ADJUSTING", action: "on", icon: iconsUrl+"breeze4h_blk.png", backgroundColor: "#2179b8", nextState: "turningBreezeOn"
			state "turningBreezeOff", label:"ADJUSTING", action: "off", icon: iconsUrl+"breeze4h_off.png", backgroundColor: "#2179b8", nextState: "turningBreezeOff"
		}
		childDeviceTile("fanLight", "fanLight", height: 2, width: 2)

		main(["switch"])
		details(["switch", "fanLight", "fanMode1", "fanMode2", "fanMode6", "fanMode3", "fanMode4", "refresh", "version"])
	}
}

def parse(String description) {
	//log.debug "Parse description $description"
	def event = zigbee.getEvent(description)
	// log.debug "event: ${event}"
	def descriptionText = ""
	if (event) {
		def childDevice = getChildDevices()?.find { it.device.deviceNetworkId == "${device.deviceNetworkId}-Light" }
		if(childDevice) {
			def value = event?.value
			def name = event?.name
			if(name == "level" && value) {
				if(value?.isNumber()) {
					descriptionText = "[${childDevice?.displayName}] | Light Level Set to (${value}%)"
					sendEvent(name: "lightLevel", value: value, descriptionText: descriptionText)
				}
			}
			if(name == "switch" && value) {
				if(value == "on" || value == "off") {
					descriptionText = "[${childDevice?.displayName}] | Light Switch Set to (${value})"
					sendEvent(name: "lightStatus", value: value, descriptionText: descriptionText)
				}
			}
			if(value && childDevice?.currentState(name)?.stringValue != value?.toString() && descriptionText?.length() > 0) { log.debug "${descriptionText}" }
			childDevice?.sendEvent(event)	//send light events to light child device and update lightLevel attribute
		}
	} else {
	   	// log.info "Fan event detected on controller" 
		if (description?.startsWith("read attr -")) {
			def descMap = zigbee.parseDescriptionAsMap(description)
			if (descMap.cluster == "0202" && descMap.attrId == "0000") {     // Fan Control Cluster Attribute Read Response
				def name = "fanMode"
				def value = descMap?.value
				if(value) {
					def strValue = getFanModeMap()[value.toString()]
					// log.debug "value: $value | strValue: $strValue | curSwitch: ${device?.currentValue("switch")} ${(device?.currentValue("switch") != (strValue == "Off" ? "off" : "on"))}"
					if(strValue) {
						if (device?.currentValue("switch") != (strValue == "Off" ? "off" : "on")) {
							sendEvent(name:"switch", value: (strValue == "Off" ? "off" : "on"), descriptionText: "${device.displayName} | Switch (${strValue == "Off" ? "OFF" : "ON"})")
							descriptionText = "${device.displayName} | Fan (${strValue == "off" ? "OFF" : "ON"})"
						}
						if (isStateChange(device, "fanSpeedDesc", strValue?.toString())) {
							sendEvent(name:"fanSpeedDesc", value: strValue, descriptionText: descriptionText)
						}
					}
					if(value && device?.currentState(name)?.stringValue != value?.toString() && descriptionText?.length() > 0) { log.debug "${descriptionText}" }
					sendEvent(name:"fanMode", value: value)
					// fanSync(value)
				}
			}
		}	// End of Read Attribute Response
   	}
}

def getFanModeMap(abbr=false) {
	[
	"00":"Off",
	"01":"Low",
	"02":"Med",
	"03":"Med-Hi",
	"04":"High",
	"05":"Off",
	"06": (abbr ? "Breeze\u2122" : "Comfort Breeze\u2122"),
	"07":"Light",
	"Off":"00",
	"Low":"01",
	"Med":"02",
	"Med-Hi":"03",
	"High":"04",
	"${(abbr ? "Breeze\u2122" : "Comfort Breeze\u2122")}":"06"
	]
}

def installed() {
	initialize()
}

def updated() {
	if(state.oldLabel != device.label) {updateChildLabel()}
		initialize()
}

def initialize() {
	log.info "Initializing"
	   	if(refreshChildren) {
			deleteChildren()
			device.updateSetting("refreshChildren", false)
		}
		else {
			// createFanChild()
			createLightChild()
			response(refresh() + configure())
		}
}

def updateChildLabel() {
	log.info "UPDATE LABEL"
	def childDeviceL = getChildDevices()?.find { it.device.deviceNetworkId == "${device.deviceNetworkId}-Light" }
	if (childDeviceL) {childDeviceL.label = "${device.displayName}-Light"}    // rename with new label
}

def createLightChild() {
	def childDevice = getChildDevices()?.find { it.device.deviceNetworkId == "${device.deviceNetworkId}-Light" }
	if (!childDevice) {
		childDevice = addChildDevice("KOF Zigbee Fan Controller - Light Child Device", "${device.deviceNetworkId}-Light", null,[completedSetup: true, label: "${device.displayName} Light", isComponent: false, componentName: "fanLight", componentLabel: "Light", "data":["parent version":version]])
		log.info "Creating child light ${childDevice}"
	}
	else {
		log.info "Child already exists"
	}
}

def deleteChildren() {
	def children = getChildDevices()
	children.each {child->
  		deleteChildDevice(child.deviceNetworkId)
	}
	log.info "Deleting children"
}

def configure() {
	log.info "Configuring Reporting and Bindings."
	def cmds = [
		//Bindings
		"zdo bind 0x${device.deviceNetworkId} 1 1 0x0006 {${device.zigbeeId}} {}", "delay 100",
		"zdo bind 0x${device.deviceNetworkId} 1 1 0x0008 {${device.zigbeeId}} {}", "delay 100",
		"zdo bind 0x${device.deviceNetworkId} 1 1 0x0202 {${device.zigbeeId}} {}", "delay 100",
		//reporting
		"st cr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0006 0 0x10 1 7200 {}","delay 100",
		"st cr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0008 0 0x20 1 7200 {}", "delay 100",
		"st cr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0202 0 0x30 1 7200 {}", "delay 100",
	] + refresh()
	return cmds
}

def on() {
	log.info "Resuming Previous Fan Speed"
	def lastFan =  device.currentValue("lastFanMode")	 //resumes previous fanspeed
	return adjFanSpeed("$lastFan")
}

def off() {
	def fanNow = device.currentValue("fanMode")    //save fanspeed before turning off so it can be resumed when turned back on
	if(fanNow != "00") { sendEvent("name":"lastFanMode", "value":fanNow) }  //do not save lastfanmode if fan is already off
	def cmds = [
		"st wattr 0x${device.deviceNetworkId} 1 0x202 0 0x30 {00}"
	]
	log.info "Turning fan Off"
	return cmds
}

def lightOn()  {
	log.info "Turning Light On"
	zigbee.on()
}

def lightOff() {
	log.info "Turning Light Off"
	zigbee.off()
}

def lightLevel(val) {
	log.info "Adjusting Light Brightness"
	sendEvent(name: "lightLevel", value: val)
	zigbee.setLevel(val) + (val?.toInteger() > 0 ? zigbee.on() : [])
}

def adjFanSpeed(speed) {
	log.info "Adjusting Fan Speed to "+ getFanModeMap()[speed]
	def cmds=[ "st wattr 0x${device.deviceNetworkId} 1 0x202 0 0x30 {${speed}}" ]
	sendEvent(name: "level", value: getFanModeToLevel(speed))
	return cmds
}

def fanSync(whichFan) {
	if(whichFan == "00") {
		sendEvent(name:"switch",value:"off") //send OFF event to Fan Parent
	}
}

def ping() {
	return zigbee.onOffRefresh()
}

def refresh() {
	getChildVer()
	return [
        "st rattr 0x${device.deviceNetworkId} 1 0x006 0","delay 100",  //light state
        "st rattr 0x${device.deviceNetworkId} 1 0x008 0","delay 100",  //light level
        "st rattr 0x${device.deviceNetworkId} 1 0x202 0"               //fan state
    ]
}

def getChildVer() {
	def LchildDevice = getChildDevices()?.find { it.device.deviceNetworkId == "${device.deviceNetworkId}-Light" }
	if(LchildDevice) {	    //find the light device, get version info and store in LchildVer
		sendEvent(name:"LchildVer", value: LchildDevice.version)
		sendEvent(name:"FchildCurr", value: (LchildDevice.version != childLightVer ? 1 : 2))
	}
}

def setFanSpeed(value) {
	setLevel(value)
}

def getFanModeToLevel(mode) {
	switch(mode) {
		case "01":
			return 25
		case "02":
			return 50
		case "03":
			return 75
		case "04":
			return 99
		default:
			return 0
	}
}

def setLevel(value) {
	def lowvalue =  25
	def medvalue = 50
	def medhighvalue = 75
	def highvalue = 99
	if (["LOW","low", "Low"].contains(value)) { value = lowvalue }
	if (["MED","med", "medium", "Med", "Medium"].contains(value)) { value = medvalue }
	if (["MEDHIGH","medhigh", "mediumhigh", "Med-High", "MediumHigh"].contains(value)) { value = medhighvalue }
	if (["HIGH","high", "High"].contains(value)) { value = highvalue }

	value = value as Integer
	// log.trace "setLevel(value): ${value} | fan: ${fanSpeedConversion(value)}"
	if(value?.isNumber()) {
		sendEvent(name:"level", value:value)
		adjFanSpeed(fanSpeedConversion(value)?.toString())
	}
}

def lowSpeed() {
	// log.trace "lowSpeed()"
	adjFanSpeed("01")
}

def medSpeed() {
	// log.trace "medSpeed()"
	adjFanSpeed("02")
}

def medHighSpeed() {
	// log.trace "medHighSpeed()"
	adjFanSpeed("03")
}

def highSpeed() {
	// log.trace "highSpeed()"
	adjFanSpeed("04")
}

def breeze() {
	// log.trace "breeze()"
	adjFanSpeed("06")
}

def fanSpeedConversion(Integer speedVal) {
	if (speedVal > 0 && speedVal <=25) {
		return "01";
	} else if (speedVal > 25 && speedVal <= 50) {
		return "02";
	} else if (speedVal > 50 && speedVal <= 75) {
		return "03";
	} else if (speedVal > 75 && speedVal <= 100) {
		return "04";
	} else {
		return "00";
	}
}
