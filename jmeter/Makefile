APP=jmeter

include $(MAKEPATH)/Makefile

compile:
		./build.sh

marathon: marathon_app
	@echo no-op

deploy:
					curl -X POST $(MARATHON_PARAMS)\
					$(MARATHON_URL) \
					$(MARATHON_USER_AUTH) \
					-d @marathon.json -H 'Content-type: application/json' ; \


undeploy:
				curl -X DELETE $(MARATHON_PARAMS)\
					$(MARATHON_URL)/$(GROUP)/$(ENV)/jmeter \
					$(MARATHON_USER_AUTH) ; \

				curl -X DELETE $(MARATHON_PARAMS)\
					$(MARATHON_URL)/$(APPID) \
					$(MARATHON_USER_AUTH) ; \
