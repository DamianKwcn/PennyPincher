docker pull daimyo0/pennypincherdb:1.0
docker pull daimyo0/pennypincherapp:1.0

docker run `
  --name pennypincherdb `
  -e MYSQL_ROOT_PASSWORD=1234 `
  -e MYSQL_DATABASE=PennyPincher `
  -p 3306:3306 `
  -d daimyo0/pennypincherdb:1.0

docker exec -it pennypincherdb bash -c "until mysqladmin ping -h localhost --silent; do echo 'Waiting for database...'; sleep 2; done; echo 'Database is ready'"

docker run `
  --name pennypincherapp `
  -e SPRING_DATASOURCE_URL=jdbc:mysql://pennypincherdb:3306/PennyPincher `
  -e SPRING_DATASOURCE_USERNAME=root `
  -e SPRING_DATASOURCE_PASSWORD=1234 `
  -p 8080:8080 `
  --link pennypincherdb `
  -d daimyo0/pennypincherapp:1.0
