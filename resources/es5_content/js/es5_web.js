'use esversion: 6';

var ESStackName = 'EchoSistantV5';
var functionArn = '';
var echoSistantAPIURL = '';

function makeRequest(url, method, message, appId = null, appDesc = null, contentType = null, responseType = null) {
    return new Promise(function(resolve, reject) {
        var xhr = new XMLHttpRequest();
        url += appId || '';
        xhr.onreadystatechange = function() {
            if (xhr.readyState === XMLHttpRequest.DONE) {
                if (xhr.status === 200) {
                    if (appId !== null && appDesc !== null) {
                        // console.log(xhr.response);
                        resolve({
                            response: xhr.response,
                            appId: appId,
                            appDesc: appDesc
                        });
                    } else {
                        resolve(xhr.response);
                    }
                } else {
                    reject(Error(xhr.statusText));
                }
            }
        };
        xhr.onprogress = function() {
            // console.log('LOADING', xhr.readyState); // readyState will be 3
        };
        xhr.onerror = function() {
            if (appId !== null && appDesc !== null) {
                reject({
                    statusText: xhr.statusText,
                    appId: appId,
                    appDesc: appDesc
                });
            } else {
                reject(Error('XMLHttpRequest failed; error code:' + xhr.statusText));
            }
        };
        xhr.open(method, url, true);
        if (contentType !== null && responseType !== null) {
            xhr.setRequestHeader('Accept', contentType);
            xhr.responseType = responseType;
            xhr.send();
        } else {
            xhr.send(message);
        }
    });
}

function addResult(str, good) {
    // $('#results').css({ display: 'none' });
    $('#resultList').css({
        display: 'block'
    });
    $('#resultsTitle').css({
        display: 'block'
    });
    let s = '<li><span style=\'color: ' + (good !== false ? '#25c225' : '#FF0000') + ';\'>';
    s += '<i class=\'fa fa-' + (good !== false ? 'check' : 'exclamation') + '\'></i>';
    s += '</span> ' + str + '</li>';
    $('#resultList ul').append(s);
}

function installError(err, reload = true) {
    if (reload) {
        if (sessionStorage.refreshCount < 7) {
            loaderFunc();
        } else {
            installComplete(err, true);
        }
    }
}

function installComplete(text, red = false) {
    loaderDiv.style.display = 'none';
    $('#loaderDiv').css({
        display: 'none'
    });
    $('#finishedImg').removeClass('fa-exclamation-circle').addClass('fa-check').css({
        display: 'block'
    });
    if (red) {
        $('#finishedImg').removeClass('fa-check').addClass('fa-exclamation-circle').css({
            color: 'red'
        });
    }
    $('#results').css({
        display: 'block'
    }).html(text + '<br/><br/>Press Back/Done Now');
    sessionStorage.removeItem('appsDone');
    sessionStorage.removeItem('refreshCount');
}

function checkLambda(functionName) {
    try {
        var lambda = new AWS.Lambda({
            accessKeyId: accessKeyId,
            secretAccessKey: secretAccessKey,
            region: userRegion
        });
        lambda.getFunction({
            FunctionName: functionName
        }, function(err, data) {
            if (err) {
                installAwsError(err, 'Lambda Check Failed');
            } else {
                addResult('Lambda Verified', true);
                functionArn = data.Configuration.EchoSistantV5;
            }
        });
    } catch (err) {
        installAwsError(err, 'Lambda Check Failed');
    }
}

function installAwsError(err, results) {
    $('#errResults').html(err);
    $('#loaderDiv').css({
        display: 'none'
    });
    $('#finishedImg').removeClass('fa-check').addClass('fa-exclamation-circle').css({
        color: 'red',
        display: 'block'
    });
    $('#results').css({
        display: 'block'
    }).html('<br/>' + results);
}

function installAwsComplete(text) {
    var lambdaOutput = JSON.stringify({
        APIURL: echoSistantAPIURL,
        ARN: functionArn,
        stackVersion: '5.0.0106'
    });
    makeRequest(stAwsResp, 'POST', lambdaOutput)
        .catch(function(err) {
            installAwsError(err, 'AWS Results Upload<br/>Failed!');
        })
        .then(function(resp) {
            // console.log("install data to ST response: ", resp);
            if (JSON.parse(resp).gotData) {
                $('#loaderDiv').css({
                    display: 'none'
                });
                $('#finishedImg').removeClass('fa-exclamation-circle').addClass('fa-check').css({
                    display: 'block'
                });
                $('#results').css({
                    display: 'block'
                }).html('<br/>' + text + '<br/><br/>Press Back/Done Now');
            }
        });
}

function stUpdates() {
    let appsDone = [];
    $('#loaderText1').text('Authenticating');
    $('#loaderText2').text('Please Wait');
    makeRequest(authUrl, 'GET', null)
        .then(function(response) {
            $('#results').text('');
            addResult('SmartThings Authentication', true);
            $('#loaderText2').text('Checking');
            $('#loaderText1').text('Lambda');
            makeRequest(zipUrl, 'GET', null, null, null, 'application/zip', 'arraybuffer')
                .catch(function(err) {
                    installError(err, false);
                    installComplete('Lambda Update<br/>Failed!', true);
                })
                .then(function(resp) {
                    $('#loaderText1').text('Updating');
                    $('#loaderText2').text('Lambda');
                    // console.log(resp);
                    var lambda = new AWS.Lambda({
                        accessKeyId: accessKeyId,
                        secretAccessKey: secretAccessKey,
                        region: userRegion
                    });
                    lambda.updateFunctionCode({
                        FunctionName: 'EchoSistantV5',
                        ZipFile: resp
                    }, function(err, data) {
                        if (err) {
                            installError(err, false);
                            addResult('Lambda Install Error', false);
                        } else {
                            if (data) {
                                addResult(Math.round(parseInt(data.CodeSize) / 1000) + 'KB Lamba Zip Uploaded', true);
                            }
                            addResult('Lambda was Updated', true);
                            lambdaDone = true;
                            if (appsDone.length < Object.keys(appIds).length) {
                                for (var i in appIds) {
                                    var appDesc = i.toString();
                                    let appId = appIds[i];
                                    let appType;
                                    // console.log('addDesc: '+appDesc)
                                    if (appDesc !== undefined) {
                                        if (appDesc.toString() === 'main') {
                                            appType = 'Main App';
                                        } else if (appDesc.toString() === 'profile') {
                                            appType = 'Profile App';
                                        } else if (appDesc.toString() === 'storage') {
                                            appType = 'Storage App';
                                        } else if (appDesc.toString() === 'shortcut') {
                                            appType = 'Shortcut App';
                                        }
                                    }
                                    $('#loaderText1').text('Checking');
                                    $('#loaderText2').text(appType);
                                    makeRequest(upd1Url, 'GET', null, appId, appType)
                                        .catch(function(errResp1) {
                                            installError(errResp1, false);
                                            addResult(errResp1.appDesc + ' Update Issue', false);
                                        })
                                        .then(function(stResp1) {
                                            // console.log(stResp1);
                                            let respData = JSON.parse(stResp1.response);
                                            if (respData.hasDifference === true) {
                                                $('#loaderText1').text('Updating');
                                                $('#loaderText2').text(stResp1.appDesc);
                                                makeRequest(upd2Url, 'GET', null, stResp1.appId, stResp1.appDesc)
                                                    .catch(function(errResp2) {
                                                        installError(errResp2, false);
                                                        addResult(errResp2.appDesc + ' Update Issue', false);
                                                    })
                                                    .then(function(stResp2) {
                                                        if (!JSON.parse(stResp2.response).errors.length) {
                                                            $('#loaderText1').text('Compiling');
                                                            $('#loaderText2').text(stResp2.appDesc);
                                                            // console.log("stResp2(" + stResp2.appId + "):", JSON.parse(stResp2.response));
                                                            makeRequest(upd3Url, 'GET', null, stResp2.appId, stResp2.appDesc)
                                                                .catch(function(errResp3) {
                                                                    addResult(errResp3.appDesc + ' Update Issue', false);
                                                                    installError(errResp3, false);
                                                                })
                                                                .then(function(stResp3) {
                                                                    // console.log("stResp3(" + stResp3.appId + "):", JSON.parse(stResp3.response));
                                                                    addResult(stResp3.appDesc + ' was Updated', true);
                                                                    appsDone.push(stResp3.appDesc);
                                                                    sessionStorage.setItem('appsDone', appsDone);
                                                                    if (appsDone.length === Object.keys(appIds).length) {
                                                                        installComplete('Updates are Complete!<br/>Everything is Good!');
                                                                    }
                                                                });
                                                        }
                                                    });
                                            } else {
                                                addResult(stResp1.appDesc + ' was Up-to-Date', true);
                                                appsDone.push(stResp1.appDesc);
                                                sessionStorage.setItem('appsDone', appsDone);
                                                if (appsDone.length === Object.keys(appIds).length) {
                                                    installComplete('Updates are Complete!<br/>Everything is Good!');
                                                }
                                            }
                                        });
                                }
                            }
                        }
                    });
                });
        })
        .catch(function(err) {
            installError(err);
        });
}

function getRooms() {
    $('#loaderText1').text('Authenticating');
    $('#loaderText2').text('Please Wait');
    makeRequest(authUrl, 'GET', null)
        .then(function(response) {
            $('#results').text('');
            addResult('SmartThings Authentication', true);
            $('#loaderText1').text('Collecting');
            $('#loaderText2').text('Room Data');
            makeRequest(roomsUrl, 'GET', null)
                .catch(function(err) {
                    installError(err, false);
                    addResult('Room Collection Issue', false);
                })
                .then(function(resp) {
                    let locRooms = JSON.parse(resp).filter(room => room.locationId === stLocId);
                    // console.log(locRooms);
                    addResult('(' + locRooms.length + ') Rooms Fetched', true);
                    $('#loaderText1').text('Uploading');
                    $('#loaderText2').text('Room Data');

                    makeRequest(sendRoomUrl, 'POST', JSON.stringify({
                            rooms: locRooms
                        }))
                        .catch(function(err) {
                            installError(err, false);
                            addResult('Room Upload Failed', false);
                            installComplete('Room Collection<br/>Failed!', true);
                        })
                        .then(function(sendResp) {
                            // console.log(sendResp);
                            let stResp = JSON.parse(sendResp);
                            let msg = stResp.msg;
                            addResult('Data Uploaded to Echosistant', stResp.gotRooms === true);
                            installComplete(msg.length > 0 ? msg : 'Room Collection<br/>Completed!');
                        });
                });
        })
        .catch(function(err) {
            installError(err);
        });
}

function lambdaUtil() {
    try {
        $('#loaderText1').text('Checking');
        $('#loaderText2').text('Lambda');
        var cloudformation = new AWS.CloudFormation({
            accessKeyId: accessKeyId,
            secretAccessKey: secretAccessKey,
            region: userRegion
        });
        var lambda = new AWS.Lambda({
            accessKeyId: accessKeyId,
            secretAccessKey: secretAccessKey,
            region: userRegion
        });
        cloudformation.describeStacks({
            StackName: ESStackName
        }, function(err, data) {
            if (err) {
                if (err.message === 'Stack with id EchoSistantV5 does not exist') {
                    $('#loaderText1').text('Installing');
                    $('#loaderText2').text('AWS Stack');
                    $('#results').html('Please Be Patient<br/>This process takes a few minutes');
                    var stackParams = {
                        StackName: ESStackName,
                        DisableRollback: false,
                        TemplateBody: stackTemplate,
                        TimeoutInMinutes: 15
                    };
                    cloudformation.createStack(stackParams, function(err, data) {
                        if (err) {
                            installAwsError(err, 'AWS Stack Install<br/>Failed');
                        } else {
                            cloudformation.waitFor('stackCreateComplete', {
                                StackName: ESStackName
                            }, function(err, data) {
                                if (err) {
                                    installAwsError(err, 'AWS Stack Install<br/>Failed');
                                } else {
                                    functionArn = data.Stacks[0].Outputs.find(x => x.OutputKey === 'EchoSistantV5Function').OutputValue;
                                    echoSistantAPIURL = data.Stacks[0].Outputs.find(x => x.OutputKey === 'APIURL').OutputValue;
                                    addResult('Stack Created', true);
                                    $('#loaderText1').text('Downloading');
                                    $('#loaderText2').text('Lambda');
                                    makeRequest(zipUrl, 'GET', null, null, null, 'application/zip', 'arraybuffer')
                                        .catch(function(err) {
                                            installAwsError(err, 'AWS Stack Install<br/>Failed');
                                        })
                                        .then(function(resp) {
                                            $('#loaderText1').text('Installing');
                                            $('#loaderText2').text('Lambda');
                                            lambda.updateFunctionCode({
                                                    FunctionName: 'EchoSistantV5',
                                                    ZipFile: resp
                                                },
                                                function(err, data) {
                                                    if (err) {
                                                        installAwsError(err, 'AWS Stack Install<br/>Failed');
                                                    } else {
                                                        if (data) {
                                                            addResult(Math.round(parseInt(data.CodeSize) / 1000) + 'KB Lamba Zip Uploaded', true);
                                                            //console.log('data1: ', data);
                                                        }
                                                        addResult('Lambda Created', true);
                                                        installAwsComplete('AWS Stack Installed<br/>Completed');
                                                    }
                                                }
                                            );
                                        });
                                }
                            });
                        }
                    });
                } else {
                    installAwsError(err, 'AWS Stack Install<br/>Failed');
                }
            } else {
                data.Stacks.find(function(value) {
                    if (value.StackName === ESStackName && value.StackStatus === 'CREATE_COMPLETE') {
                        addResult('Existing Stack Found', true);
                        functionArn = value.Outputs.find(x => x.OutputKey === 'EchoSistantV5Function').OutputValue;
                        echoSistantAPIURL = value.Outputs.find(x => x.OutputKey === 'APIURL').OutputValue;
                    }
                });
                $('#loaderText1').text('Downloading');
                $('#loaderText2').text('Lambda');
                makeRequest(zipUrl, 'GET', null, null, null, 'application/zip', 'arraybuffer')
                    .catch(function(err) {
                        installAwsError(err, 'Lambda Zip Download<br/>Failed!');
                    })
                    .then(function(resp) {
                        $('#loaderText1').text('Updating');
                        $('#loaderText2').text('Lambda');
                        addResult('Latest Lambda Downloaded', true);
                        lambda.updateFunctionCode({
                            FunctionName: ESStackName,
                            ZipFile: resp
                        }, function(err, data) {
                            if (err) {
                                installAwsError(err, 'Lambda Update<br/>Failed!');
                            } else {
                                if (data) {
                                    addResult('Uploaded ' + Math.round(parseInt(data.CodeSize) / 1000) + 'KB Update Zip', true);
                                    //console.log('data1: ', data);
                                    lambda.updateFunctionConfiguration({
                                        FunctionName: ESStackName,
                                        Environment: {
                                            Variables: {
                                                stPath: stPath,
                                                stHost: stHost
                                            }
                                        }
                                    }, function(err, data) {
                                        if (err) {
                                            installAwsError(err, 'Lambda Update<br/>Failed!');
                                        } else {
                                            if (data) {
                                                //console.log('data2: ', data);
                                                addResult('Lambda Code is Up-to-Date', true);
                                                installAwsComplete('Lambda Update<br/>Completed!');
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    });
            }
        });
    } catch (err) {
        installAwsError(err, 'Lambda Update<br/>Failed!');
    }
}

async function loaderFunc() {
    if (functionType !== 'stackUtil') {
        $('#results').text('Waiting for connection...');
        if (sessionStorage.refreshCount === undefined) {
            sessionStorage.refreshCount = '0';
        }
        sessionStorage.refreshCount = Number(sessionStorage.refreshCount) + 1;
        switch (functionType) {
            case 'updates':
                await stUpdates();
                break;
            case 'rooms':
                await getRooms();
                break;
        }
    } else {
        await lambdaUtil();
    }
}

window.onload = loaderFunc;