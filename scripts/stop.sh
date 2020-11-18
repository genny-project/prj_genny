#!/bin/bash
ENV_FILE=../genny.env docker-compose -f docker-compose-files/docker-compose.yml  stop $@
ENV_FILE=../genny.env docker-compose -f docker-compose-files/docker-compose.yml  rm -f $@

