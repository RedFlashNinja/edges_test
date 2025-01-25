FROM grafana/grafana:latest

EXPOSE 3000 10000

COPY grafana.ini /etc/grafana/grafana.ini

ENV TZ=Europe/Prague

CMD ["grafana-server", "--config=/etc/grafana/grafana.ini", "--homepath=/usr/share/grafana"]
