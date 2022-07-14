/*사용자에게 받은 데이터(토큰값)를 커스텀토큰값으로 바꿔줌 */

//토큰 검증 및 발급 

const path = require('path');
require('dotenv').config({
    path: path.join(__dirname, '.env')
});

// 구글 토큰 검증 
const {
    OAuth2Client
} = require('google-auth-library');


//카카오 토큰검증을 위함.
const requestMeUrl = 'https://kapi.kakao.com/v2/user/me';
const request = require('request-promise');

//파이어 베이스 초기화 

const {firebaseAdmin} = require("../firebaseProcess");



//for firebase
//커스텀 토큰 생성 



function createFirebaseToken(AccessToken, platform) {
//구글 계정인 경우 

    if (platform == "google") {

    } 
    
//카카오 계정인 경우 
    else if (platform == "kakao") {
        return requestMe(AccessToken).then((response) => {
            const body = JSON.parse(response);
            console.log(body);
            const userId = `kakao:${body.id}`;
            if (!body.id) {
                return res.status(404)
                    .send({
                        message: 'There was no user with the given access token.'
                    });
            }
            let nickname = null;
            let profileImage = null;
            if (body.properties) {
                nickname = body.properties.nickname;
                profileImage = body.properties.profile_image;
            }
            return updateOrCreateUser(userId, body.kaccount_email, nickname,
                profileImage, platform);
        }).then((userRecord) => {
            const userId = userRecord.uid;
            console.log(`creating a custom firebase token based on uid ${userId}`);
            return firebaseAdmin.auth().createCustomToken(userId, {
                provider: platform
            });
        })
    }
};

//사용자 계정을 업데이트하거나 생성하는 메커니즘 
function updateOrCreateUser(userId, email, displayName, photoURL, platform) {
    console.log('updating or creating a firebase user');
    const updateParams = {
        provider: platform,
        displayName: displayName,
    };
    if (displayName) {
        updateParams['displayName'] = displayName;
    } else {
        updateParams['displayName'] = email;
    }
    if (photoURL) {
        updateParams['photoURL'] = photoURL;
    }
    console.log(updateParams);
    //파이어베이스에 사용자 등록 
    return firebaseAdmin.auth().updateUser(userId, updateParams)
        .catch((error) => {
            if (error.code === 'auth/user-not-found') {
                updateParams['uid'] = userId;
                if (email) {
                    updateParams['email'] = email;
                }
                return firebaseAdmin.auth().createUser(updateParams);
            }
            throw error;
        });
};

//토큰값 검증 
function requestMe(kakaoAccessToken) {
    var toss = 'Bearer ' + kakaoAccessToken;
    console.log('Requesting user profile from Kakao API server.');
    console.log("token " + toss);
    return request({
        method: 'GET',
        headers: {
            'Authorization': toss,
            'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8'
        },
        url: requestMeUrl,
    });
};

module.exports = {
    createFirebaseToken
};