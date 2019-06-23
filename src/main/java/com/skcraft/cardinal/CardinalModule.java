package com.skcraft.cardinal;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.skcraft.cardinal.api.WebService;
import com.skcraft.cardinal.service.claim.ClaimMap;
import com.skcraft.cardinal.service.claim.DatabaseClaimMap;
import com.skcraft.cardinal.service.hive.DefaultHive;
import com.skcraft.cardinal.service.hive.Hive;
import com.skcraft.cardinal.service.notice.NoticeManager;
import com.skcraft.cardinal.service.notice.WebNoticeManager;
import com.skcraft.cardinal.service.party.DatabasePartyManager;
import com.skcraft.cardinal.service.party.PartyManager;
import com.skcraft.cardinal.profile.DatabaseMojangIdMapping;
import com.skcraft.cardinal.profile.MojangIdMapping;
import com.skcraft.cardinal.service.remotecommand.DefaultRemoteCommandManager;
import com.skcraft.cardinal.service.remotecommand.RemoteCommandManager;
import com.skcraft.cardinal.util.event.EventBus;
import com.skcraft.cardinal.util.event.MultimapEventBus;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.Properties;

import static com.google.common.base.Preconditions.*;

public class CardinalModule extends AbstractModule {
    private final Properties properties;

    public CardinalModule(Properties properties) {
        checkNotNull(properties, "properties");
        this.properties = properties;
    }

    @Override
    protected void configure() {
        Names.bindProperties(binder(), properties);
        bind(EventBus.class).to(MultimapEventBus.class).in(Singleton.class);
        bind(WebService.class).in(Singleton.class);
        bind(RemoteCommandManager.class).to(DefaultRemoteCommandManager.class).in(Singleton.class);
        bind(Hive.class).to(DefaultHive.class).in(Singleton.class);
        bind(MojangIdMapping.class).to(DatabaseMojangIdMapping.class).in(Singleton.class);
        bind(ClaimMap.class).to(DatabaseClaimMap.class).in(Singleton.class);
        bind(PartyManager.class).to(DatabasePartyManager.class).in(Singleton.class);
        bind(NoticeManager.class).to(WebNoticeManager.class).in(Singleton.class);
    }

    @Provides
    protected DataSource provideDataSource(@Named("dataSource.url") String url,
                                           @Named("dataSource.username") String username,
                                           @Named("dataSource.password") String password) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        return ds;
    }
}
