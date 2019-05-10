FROM gennyproject/checkrules:release-2.0.6 
COPY ./rules /rules 
ENTRYPOINT ["/app.sh"]
CMD []
