FROM gennyproject/checkrules: v2.3.0
COPY ./rules /rules 
ENTRYPOINT ["/app.sh"]
CMD []
