#!/bin/bash

ENV_FILE=../genny.env

ENV_FILE=$ENV_FILE docker-compose -f docker-compose-files/docker-compose.yml logs -f $@ 
