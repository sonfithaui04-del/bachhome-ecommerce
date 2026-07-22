@echo off
echo ========================================
echo   BUILDING ALL SERVICES JAR FILES
echo ========================================

echo Building Eureka Server...
cd eureka-server && mvn clean package -DskipTests && cd ..

echo Building API Gateway...
cd api-gateway && mvn clean package -DskipTests && cd ..

echo Building Auth Service...
cd service-auth && mvn clean package -DskipTests && cd ..

echo Building Menu Service...
cd service-menu && mvn clean package -DskipTests && cd ..

echo Building Order Service...
cd service-order && mvn clean package -DskipTests && cd ..

echo Building Inventory Service...
cd service-inventory && mvn clean package -DskipTests && cd ..

echo Building Payment Service...
cd service-payment && mvn clean package -DskipTests && cd ..

echo Building Notification Service...
cd service-notification && mvn clean package -DskipTests && cd ..

echo Building Socket Service...
cd service-socket && mvn clean package -DskipTests && cd ..

echo ========================================
echo   BUILD COMPLETE!
echo ========================================
pause
