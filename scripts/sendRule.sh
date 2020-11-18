#!/bin/bash

rulename=$1

rulefile=$(grep -r 'id="'${rulename}'"' * | awk -F":" '{ print $1 }')

echo $rulefile
name=$(grep -o 'name="[^"]*"'  $rulefile |  head -n 1 | grep -o '".*"'  | tr -d '"')
echo "name=$name"
rule=`cat $rulefile`

# get token
myip=keycloak.genny.life
KEYCLOAK_RESPONSE=`curl -s -X POST http://${myip}:8180/auth/realms/genny/protocol/openid-connect/token  -H "Content-Type: application/x-www-form-urlencoded" -d 'username=user1' -d 'password=password1' -d 'grant_type=password' -d 'client_id=genny'  -d 'client_secret=056b73c1-7078-411d-80ec-87d41c55c3b4'`
#printf "$KEYCLOAK_RESPONSE \n\n"

TOKEN=`echo "$KEYCLOAK_RESPONSE" | jq -r '.access_token'`
ruletext='{  "msg_type":"DATA_MSG", "data_type":"Rule" ,"items":[{ "ruleGroup":"internmatch", "rule": '"${rule}"',"code":"RUL_TEST" } ] }'

MSG=${rule//\"/\\\"}
#MSG=`cat $rulefile | grep -o ' .\+' | jq -aR .`
JSON='{  "msg_type":"DATA_MSG", "data_type":"Rule" ,"items":[{ "ruleGroup":"internmatch", "rule":"'"${MSG}"'","code":"RUL_TEST" } ] }'
echo $JSON > tmp.tmp
curl -k -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' --header "Authorization: Bearer $TOKEN" -d @tmp.tmp 'http://keycloak.genny.life:8088/api/service' # load rule into json


