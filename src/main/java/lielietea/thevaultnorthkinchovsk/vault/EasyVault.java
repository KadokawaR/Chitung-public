package lielietea.thevaultnorthkinchovsk.vault;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class EasyVault implements Vault<Long,Currency> {
    private final static Type TYPE = new TypeToken<VaultCore<Long,Currency>>() {}.getType();
    private VaultCore<Long,Currency> core;
    private final Lock lock = new ReentrantLock(true);
    private final File serializePath;
    private final BalanceDisplayFormat format;
    private final Gson gson;

    public EasyVault(File serializePath) {
        this(serializePath,BalanceDisplayFormat.STANDARD);
    }

    public EasyVault(File serializePath, BalanceDisplayFormat defaultDisplayFormat) {
        this.serializePath = serializePath;
        format = defaultDisplayFormat;
        gson = new GsonBuilder()
                .registerTypeAdapter(TYPE, new VaultCodec<>(new EasyVaultVaultCodecComponent()))
                .setPrettyPrinting().create();
        load();
    }

    @Override
    public boolean deposit(Long user, Currency valuable, double amount) {
        return deposit(user, valuable, new BigDecimal(amount));
    }

    @Override
    public boolean deposit(Long user, Currency valuable, BigDecimal amount) {
        if(!(amount.compareTo(BigDecimal.ZERO) >0))
            throw new IllegalArgumentException("deposit amount is invalid");
        else{
            BigDecimal newBalance = core.getBalance(user, valuable).add(amount);
            core.set(user, valuable ,newBalance);
            setChanged();
            return true;
        }
    }

    @Override
    public boolean withdraw(Long user, Currency valuable, double amount) {
        return withdraw(user, valuable, new BigDecimal(amount));
    }

    @Override
    public boolean withdraw(Long user, Currency valuable, BigDecimal amount) {
        if(!(amount.compareTo(BigDecimal.ZERO) >0))
            throw new IllegalArgumentException("withdraw amount is invalid");
        else{
            BigDecimal balance = core.getBalance(user, valuable);
            if(balance.compareTo(amount)<0){
                return false;
            }else{
                BigDecimal newBalance = core.getBalance(user, valuable).subtract(amount);
                if(newBalance.compareTo(BigDecimal.ZERO)==0)
                    core.clearBalance(user, valuable);
                else
                    core.set(user, valuable,newBalance);
                setChanged();
                return true;
            }
        }
    }

    @Override
    public void set(Long user, Currency valuable, double amount) {
        set(user, valuable, new BigDecimal(amount));
    }

    @Override
    public void set(Long user, Currency valuable, BigDecimal amount) {
        if(amount.compareTo(BigDecimal.ZERO)==0)
            core.clearBalance(user,valuable);
        else
            core.set(user, valuable, amount);
        setChanged();
    }

    @Override
    public BigDecimal get(Long user, Currency valuable) {
        return core.getBalance(user, valuable);
    }

    @Override
    public String balance(Long user, Currency valuable) {
        return balancePrecise(user,valuable,format);
    }

    @Override
    public String balancePrecise(Long user, Currency valuable, BalanceDisplayFormat format) {
        return core.getDisplayBalance(user, valuable,format);
    }

    @Override
    public boolean clearValuableOf(Long user, Currency valuable) {
        boolean flag = core.clearBalance(user,valuable);
        if(flag)
            setChanged();
        return flag;
    }

    @Override
    public boolean clearAllValuableOf(Long user) {
        boolean flag = false;
        for(Currency currency:core.getCurrentValuables()){
            if(core.clearBalance(user,currency))
                flag = true;
        }
        if(flag)
            setChanged();
        return flag;
    }

    @Override
    public void clearAllValuable() {
        core.clearVaultSpaceOnly();
        setChanged();
    }

    @Override
    public Set<Currency> getCurrentValuables() {
        return core.getCurrentValuables();
    }

    @Override
    public Set<Currency> getHistoricalValuables() {
        return core.getHistoricalValuables();
    }

    @Override
    public Set<Long> getCurrentUsers() {
        return core.getCurrentUsers();
    }

    @Override
    public Set<Long> getHistoricalUsers() {
        return core.getHistoricalUsers();
    }

    @Override
    public Optional<Map<Long, BigDecimal>> removeValuable(Currency valuable) {
        Map<Long,BigDecimal> m = core.getAllBalanceOfValuable(valuable);
        if(m.isEmpty())
            return Optional.empty();
        core.clearValuableBalance(valuable);
        setChanged();
        return Optional.of(m);
    }

    @Override
    public Optional<Map<Currency, BigDecimal>> removeUser(Long user) {
        Map<Currency,BigDecimal> m = core.getAllBalanceOfUser(user);
        if(m.isEmpty())
            return Optional.empty();
        core.clearUserBalance(user);
        setChanged();
        return Optional.of(m);
    }

    @Override
    public Optional<Map<Long, BigDecimal>> getUserOf(Currency valuable) {
        Map<Long,BigDecimal> m = core.getAllBalanceOfValuable(valuable);
        if(m.isEmpty())
            return Optional.empty();
        else
            return Optional.of(m);
    }

    @Override
    public Optional<Map<Currency, BigDecimal>> getValuablesOf(Long user) {
        Map<Currency,BigDecimal> m = core.getAllBalanceOfUser(user);
        if(m.isEmpty())
            return Optional.empty();
        else
            return Optional.of(m);
    }

    @Override
    public void reloadFromJson() {
        load();
    }

    private void setChanged() {
        save();
    }

    private void load(){
        lock.lock();
        try {
            if(!serializePath.exists()){
                try(BufferedWriter out = new BufferedWriter(new FileWriter(serializePath))){
                    out.write("{}");
                    serializePath.createNewFile();
                } catch(IOException e){
                    e.printStackTrace();
                }
            }
            try(BufferedReader in = new BufferedReader(new FileReader(serializePath))) {
                core = gson.fromJson(in, TYPE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            lock.unlock();
        }
    }

    private void save(){
        lock.lock();
        try {
            try(BufferedWriter out = new BufferedWriter(new FileWriter(serializePath))) {
                out.write(gson.toJson(core, TYPE));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            lock.unlock();
        }
    }

    static class EasyVaultVaultCodecComponent implements VaultCodecComponent<Long,Currency> {

        @Override
        public String userToJson(Long user) {
            return Long.toString(user);
        }

        @Override
        public Long userFromJson(String jsonString) {
            return Long.parseLong(jsonString);
        }

        @Override
        public Currency valuableFromJson(String jsonString) {
            return Currency.of(jsonString);
        }
    }
}
