'use esversion: 6';

function makeRequest(url, method, message, async = true) {
    return new Promise(function(resolve, reject) {
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function() {
            if (xhr.readyState === XMLHttpRequest.DONE) {
                if (xhr.status === 200) {
                    resolve(xhr.response);
                } else {
                    reject(Error(xhr.statusText));
                }
            }
        };
        xhr.onprogress = function() {
            // console.log('LOADING', xhr.readyState); // readyState will be 3
        };
        xhr.onerror = function() {
            reject(Error('XMLHttpRequest failed; error code:' + xhr.statusText));
        };
        xhr.open(method, url, async);
        xhr.send(message);
    });
}

/* [
	 Acceleration Sensor, Alarm, Audio Notification, Battery, Button, Camera, Carbon Monoxide Detector,
	 Color Control, Color Temperature, Contact Sensor, Door, Door Control, Doorbell, Energy Meter, Fan,
	 Garage Door, Garage Door Control, Harmony Activity, Harmony Hub, Holdable Button, Illuminance Measurement,
	 Image Capture, Keypad, Lamp, Light, Lock, Lock Codes, Media Controller, Minimote, Momentary, Motion Sensor,
	 Music Player, NestReport, Outlet, Power Meter, Power Source, Presence Sensor, Relative Humidity Measurement,
	 Relay Switch, Remote, Shortcut, Signal Strength, Siren, Smoke Detector, Sound Sensor, Speaker, Speech Synthesis,
	 Switch, Switch Level, Tamper Alert, Temperature Measurement, Thermostat, Three Axis, Tone, Ultraviolet Index,
	 Valve, Video Camera, Water Sensor, Window
 ]
*/

function getRandomItem(items) {
    return items[Math.floor(Math.random() * items.length)];
}

function getRandCardColor() {
    let c = ['orange', 'purple', 'peach', 'aqua', 'blue'];
    return getRandomItem(c);
}

function doNotGroupTheseCaps() {
    return ['Acceleration Sensor', 'Audio Notification', 'Color Temperature', 'Holdable Button', 'Configuration', 'Sensor', 'Actuator', 'Bridge'];
}

function getDeviceCapabList(data = null) {
    let lst = [];
    let items = data;
    for (let ad in items) {
        let caps = items[ad].capabilities.map(caps => caps);
        lst = lst + caps;
    }
    lst = _.uniq(lst).sort();
    return lst;
}

// TODO: Add location data (Current Mode / Sunrise / Sunset)
// TODO: Split the devices up and group by capabilities
// TODO: Add cap icons to the groups
// TODO: Add the current states to the device info (On/Off / Battery / Temp)

function getStContentSect() {
    let html = '';
    makeRequest(stData, 'GET', null, true)
        .catch(function(errResp) {
            console.error(errResp);
            $('#stContent').append('<p>There was an Error trying to generate the ST Content</p><br/><p>' + errResp + '</p>');
        })
        .then(function(stResp) {
            // console.log(stResp);
            let allData = JSON.parse(stResp);

            let devData = allData.devData || {};
            let profs = allData.locData.availableRooms.map(r => r.name).sort();
            let routines = allData.locData.availStRoutines.map(r => r.name).sort();
            let modes = allData.locData.availStModes.map(m => m.name).sort();

            let cnt = 1;
            if (profs.length) {
                html += '<!--Profile heading-->';
                html += '<div id="profile_list" class="row">';
                html += '   <div class="col-md-12 text-center">';
                html += '       <h1 class="h1-responsive whiteText"> Profiles/Rooms<br/>';
                html += '           <!--<small class="text-muted">' + profs.length + ' Created</small>-->';
                html += '       </h1>';
                html += '   </div>';
                html += '</div>';
                html += '<!--/.Prof heading-->';
                html += '<hr class="white">';

                html += '<!--Grid row start-->';
                html += '<div class="row wow fadeIn" data-wow-delay="0.2s">';

                for (let p in profs) {
                    let prof = profs[p];
                    let devs = devData.filter(theDev => theDev.rooms.includes(prof) && !theDev.capabilities.includes('Shortcut')).map(theDev => theDev.label).sort();

                    // let caps = getDeviceCapabList(devData);
                    // console.log('caps: ' + caps);
                    let shortcuts = devData.filter(theDev => theDev.rooms.includes(prof) && theDev.capabilities.includes('Shortcut')).map(theDev => theDev.label).sort();

                    html += ' ';
                    html += '<!-- ' + prof + 'Profile Grid column start-->';
                    html += '<div class="col-lg-4 col-md-6 mb-r">';
                    html += '   <!--Card-->';
                    html += '   <div id="profile_item_' + cnt + '" class="card card-cascade wider">';
                    html += '       <!--Card image-->';
                    html += '       <div class="view gradient-card-header ' + getRandCardColor() + '-gradient text-center">';
                    html += '           <h2 class="h2-responsive mb-2" style="font-weight: 400;">' + prof + '</h2>';
                    html += '       </div>';
                    html += '       <!--/Card image-->';
                    html += '   ';
                    html += '       <!--Card content Panel-->';
                    html += '       <div class="card card-body">';
                    html += '           <h5 class="card-title blackText" style="font-family: Roboto; font-weight: 600; margin-bottom: 3px;">Devices: ' + devs.length + '</h5>';
                    html += '           <p class="card-text" style="padding-left: 10px;">';
                    html += '               ' + devs.length ? devs.join('<br/>') : 'No Devices Found';
                    html += '           </p>';
                    if (shortcuts.length) {
                        html += '           <hr>';
                        html += '           <h5 class="card-title blackText" style="font-family: Roboto; font-weight: 600; margin-bottom: 3px;"> Shortcuts: ' + shortcuts.length || 0 + '</h5>';
                        html += '           <p class="card-text" style="padding-left: 10px;"> ' + shortcuts.length ? shortcuts.join('<br/>') : 'No Shortcuts Found' + '</p>';
                    }
                    html += '       </div>';
                    html += '       <!--Card content Panel-->';
                    html += '   ';
                    html += '   </div>';
                    html += '   <!--/.Card-->';
                    html += '</div>';
                    html += '<!-- ' + prof + 'Profile Grid column end-->';
                    cnt++;
                }
                html += '</div>';
                html += '<!--Grid row end-->';
            } else {
                html = '<p>No Profiles were returned!</p>';
            }

            if (routines.length || modes.length) {
                html += ' ';
                html += '<!--Grid row start-->';
                html += '<div id="modeandroutineRow" class="row fadeIn" >';
                html += routines.length ? getRoutinesHTML(routines) : '';
                html += modes.length ? getModesHTML(modes) : '';
            }
            // console.log(html);
            $('#stContent').append(html);
            $('#loaderDiv').css({ display: 'none' });
            new WOW().init();
        });
}

function getRoutinesHTML(routines) {
    let html = '';
    html += '<!--Routine Grid column start-->';
    html += '<div class="col-lg-4 col-md-6 mb-r">';
    html += ' ';
    html += '   <!--Routine Count Card start-->';
    html += '   <div id="routine_list" class="card card-cascade">';
    html += '       <!--Card image-->';
    html += '       <div class="view gradient-card-header ' + getRandCardColor() + '-gradient text-center">';
    html += '           <h2 class="h2-responsive" style="font-family: Roboto; font-weight: 600; margin-bottom: 3px;">SmartThings Routines</h2>';
    html += '           <p>' + routines.length + ' Routines</p>';
    html += '       </div>';
    html += '       <!--/Card image-->';
    html += '  ';
    html += '       <!--Card content-->';
    html += '       <div class="card-body">';
    html += '           <p class="card-text">' + routines.join('<br/>') + '</p>';
    html += '       </div>';
    html += '       <!--/.Card content-->';
    html += '   ';
    html += '   </div>';
    html += '   <!--Routine Count Card end-->';
    html += '</div>';
    html += '<!--Routine Grid column end-->';
    return html;
}

function getModesHTML(modes) {
    let html = '';
    html += '<!--Mode Grid column start-->';
    html += '<div class="col-lg-4 col-md-6 mb-r">';
    html += ' ';
    html += '   <!--Mode Count Card start-->';
    html += '   <div id="mode_list" class="card card-cascade">';
    html += '       <!--Card image-->';
    html += '       <div class="view gradient-card-header ' + getRandCardColor() + '-gradient text-center">';
    html += '           <h2 class="h2-responsive" style="font-family: Roboto; font-weight: 600; margin-bottom: 3px;">SmartThings Modes</h2>';
    html += '           <p>' + modes.length + ' Modes</p>';
    html += '       </div>';
    html += '       <!--/Card image-->';
    html += ' ';
    html += '       <!--Card content-->';
    html += '       <div class="card-body">';
    html += '           <p class="card-text">' + modes.join('<br/>') + '</p>';
    html += '       </div>';
    html += '       <!--/.Card content-->';
    html += ' ';
    html += '   </div>';
    html += '   <!--routine Count Card end-->';
    html += ' ';
    html += '</div>';
    html += '<!--Mode Grid column end-->';
    return html;
}