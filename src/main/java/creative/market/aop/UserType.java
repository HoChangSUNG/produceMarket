package creative.market.aop;

public enum UserType {

    SELLER("seller"),BUYER("buyer"),ADMIN("admin");

    private String name;

    UserType(String name){
        this.name = name;
    }

}
