package com.ibm.epricer.svclib.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ibm.epricer.data.pref.UserRepo;
import com.ibm.epricer.svclib.BusinessRuleException;

@Service
public class TestServiceImpl implements TestService {
    @Autowired
    private HelloService hello;
    
    @Autowired
    UserRepo userRepo;

    @Override
    public String greet(String name) throws BusinessRuleException {
        HelloInput msg = new HelloInput();
        msg.setName(name);
        try {
            return hello.greetSingleOne(msg).getGreeting();
        } catch (BusinessRuleException e) {
            throw new IllegalStateException("Could not greet one", e);
        }
    }

    @Override
    public void test() {
        // Do nothing
    }

    @Override
    public void userChangeLastname(UserDataInput input) throws BusinessRuleException {
        
        userRepo.updateUserLastname(input.getUserId(), input.getLastName());

        // Throw the exception to test the previous update roll-back  
        if (input.getUserId().equalsIgnoreCase("MT")) {
            throw new BusinessRuleException(157, "Uh-Oh, this Michael must not be changed !");
        }

        // Unchecked exceptions roll-back database changes
        if (input.getUserId().equalsIgnoreCase("MJ")) {
            throw new IllegalArgumentException("Too late to change name !");
        }
    }
}
