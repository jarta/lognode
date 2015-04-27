# lognode
build a central log bus on redis

curl -d"test=123445" http://localhost:8081/my_resource
curl -i -H 'content-type: application/json' -X POST -d '{\"test\":{\"subject\":\"tools\"}}' http://localhost:8081/daemon/publish
curl http://localhost:8081/my_resource?a=100


curl -i -H 'content-type: application/json' -X POST -d 'payload={\"payload\":{\"subject\":\"tools\"}}' http://localhost:8081/daemon/publish/itrs
curl -i -X POST -d 'payload={"subject":"ttt","log":[{"author":"nobody"}]}&tstamp=11111' http://localhost:8081/daemon/publish/itrs