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
@Field String childLightVer = "v0.3.0"

metadata {
	definition(name: "Hampton Bay Fan Controller", namespace: "tonesto7", author: "Stephan Hackett, Ranga Pedamallu, Dale Coffing") {
		capability "Actuator"
		capability "Configuration"
		capability "Refresh"
		capability "Switch"
		capability "Light"
		capability "Switch Level"
		capability "Sensor"
		capability "Polling"
        capability "Fan Speed"
		//capability "Health Check"

		command "lightOn"
		command "lightOff"
		command "lightLevel"
		command "lowSpeed"
		command "medSpeed"
		command "medHighSpeed"
		command "highSpeed"
		command "breeze"
		command "raiseFanSpeed"
		command "lowerFanSpeed"

		attribute "lightLevel", "number"	//stores brightness level
		attribute "lightStatus", "number"	//stores on/off
		attribute "lastFanMode", "string"	//used to restore previous fanmode
		attribute "LchildVer", "string"		//stores light child version
		attribute "FchildVer", "string"		//stores fan child version
		attribute "LchildCurr", "string"	//stores color of version check
		attribute "FchildCurr", "string"	//stores color of version check

		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0202", outClusters: "0003, 0019", manufacturer: "King Of Fans, Inc.", model: "HDC52EastwindFan", deviceJoinName: "Hampton Bay Fan"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0202", outClusters: "0003, 0019", manufacturer: "King Of Fans, Inc.", model: "HBUniversalCFRemote", deviceJoinName: "Hampton Bay Fan"
	}

    preferences {
        page(name: "childToRebuild", title: "This does not display on DTH preference page")
        section("section") {
            input(name: "refreshChild", type: "bool", title: "Delete & Recreate child light?\n\nTypically used after modifying the parent device name " +
                  "above to give the child light devices the new name. This process will take a few minutes to rebuld in the background. Please be patient." +
                  "\n\nPLEASE NOTE: Child Device must be removed from any smartApps BEFORE attempting this " +
                  "process or 'An unexpected error' occurs attempting to delete the child's.")
        }
    }

	tiles(scale: 2) {
		multiAttributeTile(name: "fanSpeed", type: "generic", width: 6, height: 4) {
			tileAttribute("device.fanSpeed", key: "PRIMARY_CONTROL") {
            	attributeState "00", label: "FAN OFF", action: "on", icon: iconsUrl("fan00h_grey.png"), backgroundColor: "#ffffff", nextState: "turningOn"
                attributeState "01", label: "LOW", action: "off", icon: iconsUrl("fan1h.png"), backgroundColor: "#79b821", nextState: "turningOff"
                attributeState "02", label: "MED", action: "off", icon: iconsUrl("fan2h.png"), backgroundColor: "#79b821", nextState: "turningOff"
				attributeState "03", label: "MED-HI", action: "off", icon: iconsUrl("fan3h.png"), backgroundColor: "#79b821", nextState: "turningOff"
				attributeState "04", label: "HIGH", action: "off", icon: iconsUrl("fan4h.png"), backgroundColor: "#79b821", nextState: "turningOff"
				attributeState "06", label: "BREEZE", action: "off", icon: iconsUrl("breeze4h_blk.png"), backgroundColor: "#008B64", nextState: "turningBreezeOff"
				attributeState "turningOn", label: "TURNING ON", action: "on", icon: iconsUrl("fan0h.png"), backgroundColor: "#2179b8", nextState: "turningOn"
				attributeState "turningOff", label: "TURNING OFF", action: "off", icon: iconsUrl("fan0h_grey.png"), backgroundColor: "#2179b8", nextState: "turningOff"
				attributeState "turningBreezeOff", label: "TURNING OFF", action: "off", icon: iconsUrl("breeze4h_teal.png"), backgroundColor: "#2179b8", nextState: "turningOff"
			}
            tileAttribute("device.fanSpeed", key: "VALUE_CONTROL") {
				attributeState "VALUE_UP", action: "raiseFanSpeed"
				attributeState "VALUE_DOWN", action: "lowerFanSpeed"
			}
			tileAttribute("device.lightLevel", key: "SECONDARY_CONTROL") {
				attributeState "lightLevel", label: 'Light: ${currentValue}%', icon: "st.switches.light.on"
			}
			/*tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", label: '${currentValue}%', action: "switch level.setLevel", icon: iconsUrl("fan0h.png")
			}*/
		}
		standardTile("refresh", "refresh", decoration: "flat", width: 2, height: 3) {
			state "default", label: "", action: "refresh.refresh", icon: "st.secondary.refresh"
		}
		/*valueTile("version", "version", width:4, height:1) {
			state "version", label: 'Ceiling Fan Parent\n${version}'
		}*/
		standardTile("fanSpeed1", "fanSpeed", decoration: "flat", width: 2, height: 2) {
			state "00", label: "LOW", action: "lowSpeed", icon: iconsUrl("fan1j_off.png"), backgroundColor: "#ffffff", nextState: "turningOn"
			state "01", label: "LOW", action: "off", icon: iconsUrl("fan1j_on.png"), backgroundColor: "#79b821", nextState: "turningOff"
			state "02", label: "LOW", action: "lowSpeed", icon: iconsUrl("fan1j_off.png"), backgroundColor: "#ffffff", nextState: "turningOn"
			state "03", label: "LOW", action: "lowSpeed", icon: iconsUrl("fan1j_off.png"), backgroundColor: "#ffffff", nextState: "turningOn"
			state "04", label: "LOW", action: "lowSpeed", icon: iconsUrl("fan1j_off.png"), backgroundColor: "#ffffff", nextState: "turningOn"
			state "06", label: "LOW", action: "lowSpeed", icon: iconsUrl("fan1j_off.png"), backgroundColor: "#ffffff", nextState: "turningOn"
			state "turningOn", label: "ADJUSTING", icon: iconsUrl("fan1j_on.png"), backgroundColor: "#2179b8", nextState: "turningOn"
			state "turningOff", label: "ADJUSTING", icon: iconsUrl("fan1j_off.png"), backgroundColor: "#2179b8", nextState: "turningOff"
		}
		standardTile("fanSpeed2", "fanSpeed", decoration: "flat", width: 2, height: 2) {
			state "00", label: "MED", action: "medSpeed", icon: iconsUrl("fan2j_off.png"), backgroundColor: "#ffffff", nextState: "turningOn"
			state "01", label: "MED", action: "medSpeed", icon: iconsUrl("fan2j_off.png"), backgroundColor: "#ffffff", nextState: "turningOn"
			state "02", label: "MED", action: "off", icon: iconsUrl("fan2j_on.png"), backgroundColor: "#79b821", nextState: "turningOff"
			state "03", label: "MED", action: "medSpeed", icon: iconsUrl("fan2j_off.png"), backgroundColor: "#ffffff", nextState: "turningOn"
			state "04", label: "MED", action: "medSpeed", icon: iconsUrl("fan2j_off.png"), backgroundColor: "#ffffff", nextState: "turningOn"
			state "06", label: "MED", action: "medSpeed", icon: iconsUrl("fan2j_off.png"), backgroundColor: "#ffffff", nextState: "turningOn"
			state "turningOn", label: "ADJUSTING", action: "on", icon: iconsUrl("fan2j_on.png"), backgroundColor: "#2179b8", nextState: "turningOn"
			state "turningOff", label: "ADJUSTING", action: "off", icon: iconsUrl("fan2j_off.png"), backgroundColor: "#2179b8", nextState: "turningOff"
		}
		standardTile("fanSpeed3", "fanSpeed", decoration: "flat", width: 2, height: 2) {
			state "00", label: "MED-HI", action: "medHighSpeed", icon: iconsUrl("fan3j_off.png"), backgroundColor: "#ffffff", nextState: "turningOn"
			state "01", label: "MED-HI", action: "medHighSpeed", icon: iconsUrl("fan3j_off.png"), backgroundColor: "#ffffff", nextState: "turningOn"
			state "02", label: "MED-HI", action: "medHighSpeed", icon: iconsUrl("fan3j_off.png"), backgroundColor: "#ffffff", nextState: "turningOn"
			state "03", label: "MED-HI", action: "off", icon: iconsUrl("fan3j_on.png"), backgroundColor: "#79b821", nextState: "turningOff"
			state "04", label: "MED-HI", action: "medHighSpeed", icon: iconsUrl("fan3j_off.png"), backgroundColor: "#ffffff", nextState: "turningOn"
			state "06", label: "MED-HI", action: "medHighSpeed", icon: iconsUrl("fan3j_off.png"), backgroundColor: "#ffffff", nextState: "turningOn"
			state "turningOn", label: "ADJUSTING", action: "on", icon: iconsUrl("fan3j_on.png"), backgroundColor: "#2179b8", nextState: "turningOn"
			state "turningOff", label: "ADJUSTING", action: "off", icon: iconsUrl("fan3j_off.png"), backgroundColor: "#2179b8", nextState: "turningOff"
		}
		standardTile("fanSpeed4", "fanSpeed", decoration: "flat", width: 2, height: 2) {
			state "00", label: "HIGH", action: "highSpeed", icon: iconsUrl("fan4j_off.png"), backgroundColor: "#ffffff", nextState: "turningOn"
			state "01", label: "HIGH", action: "highSpeed", icon: iconsUrl("fan4j_off.png"), backgroundColor: "#ffffff", nextState: "turningOn"
			state "02", label: "HIGH", action: "highSpeed", icon: iconsUrl("fan4j_off.png"), backgroundColor: "#ffffff", nextState: "turningOn"
			state "03", label: "HIGH", action: "highSpeed", icon: iconsUrl("fan4j_off.png"), backgroundColor: "#ffffff", nextState: "turningOn"
			state "04", label: "HIGH", action: "off", icon: iconsUrl("fan4j_on.png"), backgroundColor: "#79b821", nextState: "turningOff"
			state "06", label: "HIGH", action: "highSpeed", icon: iconsUrl("fan4j_off.png"), backgroundColor: "#ffffff", nextState: "turningOn"
			state "turningOn", label: "ADJUSTING", action: "on", icon: iconsUrl("fan4j_on.png"), backgroundColor: "#2179b8", nextState: "turningOn"
			state "turningOff", label: "ADJUSTING", action: "off", icon: iconsUrl("fan4j_off.png"), backgroundColor: "#2179b8", nextState: "turningOff"
		}
		standardTile("fanSpeed6", "fanSpeed", decoration: "flat", width: 2, height: 2) {
			state "00", label: "BREEZE", action: "breeze", icon: iconsUrl("breeze4h_off.png"), backgroundColor: "#ffffff", nextState: "turningBreezeOn"
			state "01", label: "BREEZE", action: "breeze", icon: iconsUrl("breeze4h_off.png"), backgroundColor: "#ffffff", nextState: "turningBreezeOn"
			state "02", label: "BREEZE", action: "breeze", icon: iconsUrl("breeze4h_off.png"), backgroundColor: "#ffffff", nextState: "turningBreezeOn"
			state "03", label: "BREEZE", action: "breeze", icon: iconsUrl("breeze4h_off.png"), backgroundColor: "#ffffff", nextState: "turningBreezeOn"
			state "04", label: "BREEZE", action: "breeze", icon: iconsUrl("breeze4h_off.png"), backgroundColor: "#ffffff", nextState: "turningBreezeOn"
			state "06", label: "BREEZE", action: "off", icon: iconsUrl("breeze4h_teal.png"), backgroundColor: "#79b821", nextState: "turningBreezeOff"
			state "turningBreezeOn", label: "ADJUSTING", action: "on", icon: iconsUrl("breeze4h_blk.png"), backgroundColor: "#2179b8", nextState: "turningBreezeOn"
			state "turningBreezeOff", label: "ADJUSTING", action: "off", icon: iconsUrl("breeze4h_off.png"), backgroundColor: "#2179b8", nextState: "turningBreezeOff"
		}
		childDeviceTile("fanLight", "fanLight", height: 2, width: 2)

		main(["fanSpeed"])
		details(["fanSpeed", "fanLight", "fanSpeed1", "fanSpeed2", "fanSpeed6", "fanSpeed3", "fanSpeed4", "refresh"])
	}
}

String iconsUrl(icon) {"https://cdn.rawgit.com/dcoffing/KOF-CeilingFan/master/resources/images/${icon}"}

def parse(String description) {
    log.debug "Parse description ${description}"
    def event = zigbee.getEvent(description)
    if (event) {
        log.info "Light event detected on controller: ${event}"
        getChildDevices()?.find{it.device.deviceNetworkId == "${device.deviceNetworkId}-Light"}.sendEvent(event)	//send light events to light child device and update lightBrightness attribute
        if (event.value != "on" && event.value != "off") {
            sendEvent(name: "lightLevel", value: event.value)
        }
    } else {
        log.info "Fan event detected on controller"
        def map = [:]
        if (description?.startsWith("read attr -")) {
            def descMap = zigbee.parseDescriptionAsMap(description)
            if (descMap?.cluster == "0202" && descMap?.attrId == "0000") {     // Fan Control Cluster Attribute Read Response
                map.name = "fanSpeed"
                map.value = descMap?.value
            }
        }	// End of Read Attribute Response
        if (map) {
            def result = createEvent(map)
            //def fanLevel = createEvent(name: "level", value: getFanSpeedLevel()[map.value])
            //def result = [fanMode, fanLevel]
            log.debug "Parse returned ${result}"
            return result
        }
    }
}

def getFanModeMap() {
    [
        "off": "0",
        "low": "1",
        "med": "2",
        "medium": "2",
        "med-hi": "3",
        "med-low": "3",
        "medium-hi": "3",
        "medium-low": "3",
        "high": "4",
        "breeze": "6",
        "light": "7"
    ]
}

def getFanSpeedLevel() {
    [
        "4": 100,
        "3": 75,
        "2": 50,
        "1": 25,
        "0": 0,
        "5": 0,
        "6": 1
    ]
}

def installed() {initialize()}

def updated() {initialize()}

def initialize() {
    log.info "Initializing"
    if (refreshChild) {
        log.info "Deleting Child Light"
        getChildDevices()?.each{child -> deleteChildDevice(child.deviceNetworkId)}
        device.updateSetting("refreshChild", false)
    }
    createUpdateChildLight()
    if (!state?.previousSpeed) {state.previousSpeed = "0"}
    response(refresh() + configure())
}

def createUpdateChildLight() {
	if (!getChildDevices()) {
    	log.info "Creating Child Light"
    	addChildDevice("Hampton Bay Fan Controller - Light Device", "${device.deviceNetworkId}-Light", device.hubId, [completedSetup: true, label: "${device.displayName} Light", isComponent: false, componentName: "fanLight", componentLabel: "Light", "data": ["parent version": version]])
    } else if (state.oldLabel != device.label) {
    	log.info "Updating Child Label"
    	getChildDevices()?.find{it.device.deviceNetworkId == "${device.deviceNetworkId}-Light"}.label = "${device.displayName} Light"
    }
    state.oldLabel = device.label
}

def configure() {
	log.info "Configuring Reporting and Bindings."
	return [
		//Bindings
		"zdo bind 0x${device.deviceNetworkId} 1 1 0x0006 {${device.zigbeeId}} {}", "delay 100",
		"zdo bind 0x${device.deviceNetworkId} 1 1 0x0008 {${device.zigbeeId}} {}", "delay 100",
		"zdo bind 0x${device.deviceNetworkId} 1 1 0x0202 {${device.zigbeeId}} {}", "delay 100",
		//reporting
		"st cr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0006 0 0x10 1 300 {}", "delay 100",
		"st cr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0008 0 0x20 1 300 {}", "delay 100",
		"st cr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0202 0 0x30 1 300 {}", "delay 100",
	] + refresh()
}

def on() {setFanSpeed(state.previousSpeed.toString())}

def off() {
	state.previousSpeed = device.currentValue("fanSpeed")
	setFanSpeed("0")
}

def lightOn() {zigbee.on()}

def lightOff() {zigbee.off()}

def lightLevel(val) {
	sendEvent(name: "lightLevel", value: val)
	zigbee.setLevel(val) + (val?.toInteger() > 0 ? zigbee.on() : [])
}

def setFanSpeed(speed) {
	if (speed?.size() == 1 && speed?.toString()?.isNumber()) {
    	speed = speed?.toInteger()
        if (speed > 7) {
        	speed = state?.previousSpeed
        }
    } else {
    	speed = getFanModeMap()[speed?.toLowerCase()]
    }
	log.info "Adjusting Fan Speed ${speed}"
    //response([sendEvent(name: "fanMode", value: speed), sendEvent(name: "level", value: getFanSpeedLevel()[speed])])
	return ["st wattr 0x${device.deviceNetworkId} 1 0x202 0 0x30 {0${speed}}"]
}

def ping() {zigbee.onOffRefresh()}

def refresh() {
    return zigbee.onOffRefresh() + zigbee.levelRefresh() + zigbee.readAttribute(0x0202, 0x0000)
}

def setLevel(level, rate=null) {
    if (level?.toString()?.isNumber()) {
    	if (level.toInteger() > 0) {
        	level = Math.max(Math.min(level.toInteger(), 100), 25).toFloat() // 1 ~ 37 = low, 38 ~ 62 = medium-low, 63 ~ 87 is medium, 88 ~ 100 is high
        	level = Math.round((level + 12.5f - (level + 12.5f) % 25) * 0.04f)
			setFanSpeed("0${level}")
        } else {
        	off()
        }
    }
}

def lowSpeed() {setFanSpeed("1")}

def medSpeed() {setFanSpeed("2")}

def medHighSpeed() {setFanSpeed("3")}

def highSpeed() {setFanSpeed("4")}

def breeze() {setFanSpeed("6")}

