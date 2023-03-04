package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.ConnectionRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConnectionServiceImpl implements ConnectionService {
    @Autowired
    UserRepository userRepository2;
    @Autowired
    ServiceProviderRepository serviceProviderRepository2;
    @Autowired
    ConnectionRepository connectionRepository2;

    @Override
    public User connect(int userId, String countryName) throws Exception {
        User user = userRepository2.findById(userId).get();
        if (user.getMaskedIp() != null) {
            throw new Exception("Already connected");
        } else if (countryName.equals(user.getOriginalCountry().getCountryName().toString())) {
            return user;
        } else {
            if (user.getServiceProviderList() == null) {
                throw new Exception("Unable to connect");
            }
            //Else, establish the connection where the maskedIp is "updatedCountryCode.serviceProviderId.userId" and
            // return the updated user. If multiple service providers allow you to connect to the country,
            // use the service provider having smallest id.

            //checking
            ServiceProvider serviceProvider = null;
            List<ServiceProvider> serviceProviderList = user.getServiceProviderList();
            for (ServiceProvider serviceProvider1 : serviceProviderList) {
//            if(serviceProvider1 == null || !serviceProvider1.getCountryList().contains(countryName)){
//                throw new Exception("Unable to connect");
//            }

                if ((serviceProvider.getId() > serviceProvider1.getId())) {
                    serviceProvider = serviceProvider1;
                }
            }

            if (serviceProvider != null) {

                Connection connection = new Connection();
                connection.setServiceProvider(serviceProvider);
                connection.setUser(user);

                String maskedIp = countryName + "." + serviceProvider.getId() + "." + user.getId();
                user.setMaskedIp(maskedIp);
                user.setConnected(true);


                connectionRepository2.save(connection);//to avoid duplicates

                //making connection with serviceprov
                List<Connection> connectionList = serviceProvider.getConnectionList();
                connectionList.add(connection);
                serviceProvider.setConnectionList(connectionList);
                serviceProviderRepository2.save(serviceProvider);

                //making connection with user
                List<Connection> connectionList1 = user.getConnectionList();
                connectionList1.add(connection);
                user.setConnectionList(connectionList1);
                userRepository2.save(user);
            }
        }
        return user;
    }
    @Override
    public User disconnect(int userId) throws Exception {
//If the given user was not connected to a vpn, throw "Already disconnected" exception.
        //Else, disconnect from vpn, make masked Ip as null, update relevant attributes and return updated user.
        User user = userRepository2.findById(userId).get();
        if(!user.getConnected()){
            throw new Exception("Already disconnected");
        }
        user.setMaskedIp(null);
        user.setConnected(false);
        userRepository2.delete(user);
        return user;
    }
    @Override
    public User communicate(int senderId, int receiverId) throws Exception {
        User user = userRepository2.findById(senderId).get();
        User user1 = userRepository2.findById(receiverId).get();

        if(user1.getMaskedIp()!=null){
            String str = user1.getMaskedIp();
            String cc = str.substring(0,3); //chopping country code = cc

            if(cc.equals(user.getOriginalCountry().getCode()))
                return user;
            else {
                String countryName = "";

                if (cc.equals(CountryName.IND.toCode()))
                    countryName = CountryName.IND.toString();
                if (cc.equals(CountryName.USA.toCode()))
                    countryName = CountryName.USA.toString();
                if (cc.equals(CountryName.JPN.toCode()))
                    countryName = CountryName.JPN.toString();
                if (cc.equals(CountryName.CHI.toCode()))
                    countryName = CountryName.CHI.toString();
                if (cc.equals(CountryName.AUS.toCode()))
                    countryName = CountryName.AUS.toString();

                User user2 = connect(senderId,countryName);
                if (!user2.getConnected()){
                    throw new Exception("Cannot establish communication");

                }
                else return user2;
            }

        }
        else{
            if(user1.getOriginalCountry().equals(user.getOriginalCountry())){
                return user;
            }
            String countryName = user1.getOriginalCountry().getCountryName().toString();
            User user2 =  connect(senderId,countryName);
            if (!user2.getConnected()){
                throw new Exception("Cannot establish communication");
            }
            else return user2;

        }
    }
}