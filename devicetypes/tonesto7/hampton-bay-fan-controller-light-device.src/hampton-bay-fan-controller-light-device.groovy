/**
 *  King Of Fans Zigbee Fan Controller - Light Child Device
 *
 *  Copyright 2017 Stephan Hackett
 *  in collaboration with Ranga Pedamallu, Dale Coffing
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
 def version() {return "v0.3.0"}
metadata {
	definition (name: "Hampton Bay Fan Controller - Light Device", namespace: "tonesto7", author: "Stephan Hackett") {
		capability "Actuator"
        capability "Switch"
        capability "Switch Level"
        capability "Light"
        capability "Sensor" 
   }

	tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
    		tileAttribute ("switch", key: "PRIMARY_CONTROL") {
        		attributeState "off", label:"off", action: "on", icon: getIcon()+"light_grey.png", backgroundColor: "#ffffff", nextState: "turningOn"
				attributeState "on", label: "on", action: "off", icon: getIcon()+"lightH.png", backgroundColor: "#00A0DC", nextState: "turningOff"
             	attributeState "turningOn", label:"TURNING ON", action: "on", icon: getIcon()+"lightI.png", backgroundColor: "#2179b8", nextState: "turningOn"
        		attributeState "turningOff", label:"TURNING OFF", action:"off", icon: getIcon()+"lightI.png", backgroundColor:"#2179b8", nextState: "turningOff"
        	}
    		tileAttribute ("device.level", key: "SLIDER_CONTROL") {
        		attributeState "level", label:'${currentValue}%', action: "setLevel"
    		}
		}
 		valueTile("version", "version", width: 6, height: 2) {
          	state "version", label:"\n Light Child \n" + version()+"\n"
		}
    	main(["switch"])        
		details(["switch", "rangeValue", "level", "version"])
    }	
}

def getIcon() {
	return "https://cdn.rawgit.com/dcoffing/KOF-CeilingFan/master/resources/images/"
}

def on() {
	parent.lightOn()
	sendEvent(name: "switch", value: "on")
}

def off() {
	parent.lightOff()
    sendEvent(name: "switch", value: "off")
}

def setLevel(val) {
	parent.lightLevel(val)
    sendEvent(name: "level", value: val)
}