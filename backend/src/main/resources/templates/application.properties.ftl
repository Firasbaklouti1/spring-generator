spring.application.name=${request.artifactId}
spring.datasource.url=jdbc:mysql://localhost:3306/${request.artifactId}?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
