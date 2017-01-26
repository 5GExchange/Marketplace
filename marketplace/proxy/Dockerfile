FROM nginx:1.11.4

COPY nginx.conf /etc/nginx/
COPY default.template /etc/nginx/conf.d/
COPY entrypoint.sh /usr/local/bin/
RUN chmod u+x /usr/local/bin/entrypoint.sh

CMD ["nginx", "-g", "daemon off;"]
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
