#curl -H 'Content-Type: application/json' -X DELETE -u $IBM_QA_DB_USER:$IBM_QA_DB_PASSWORD $IBM_QA_DB_URL/oc-cpd-dataecosystem-opendes-schema2
#curl -H 'Content-Type: application/json' -X DELETE -u $IBM_QA_DB_USER:$IBM_QA_DB_PASSWORD $IBM_QA_DB_URL/oc-cpd-dataecosystem-common-schema2
echo $IBM_SCHEMA_HOST
export IBM_SCHEMA_SERVICE_URL=$IBM_SCHEMA_HOST/api/schema-service/v1/schemas/system

currentStatus="success"
currentMessage="All schemas uploaded successfully"
BEARER_TOKEN=`python3 $IBM_DEPLOYMENTS_SUBDIR/Token.py`;
export BEARER_TOKEN=$BEARER_TOKEN
python deployments/scripts/DeploySharedSchemas.py -u $IBM_SCHEMA_SERVICE_URL
ret=$?
echo "Return value is $ret"
if [[ $ret -ne 0 ]]; then
	currentStatus="failure"
	currentMessage="Schema loading failed. Please check error logs for more details."
fi

echo "$currentMessage"
if [[ ${currentStatus} == "success" ]]; then
  exit 0
else
  exit 1
fi
