package com.skcraft.cardinal;

import com.google.common.collect.Lists;
import com.skcraft.cardinal.profile.DatabaseMojangIdMapping;
import com.skcraft.cardinal.profile.MojangIdMapping;
import com.skcraft.cardinal.service.claim.Claim;
import com.skcraft.cardinal.service.claim.DatabaseClaimMap;
import com.skcraft.cardinal.util.WorldVector3i;
import org.junit.Before;
import org.junit.Test;
import uk.co.it.modular.hamcrest.date.DateMatchers;

import javax.sql.DataSource;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public class DatabaseClaimsTest {

    private MockDatabase db;
    private final WorldVector3i SK_OWNED = new WorldVector3i("main", 20, 0, -30);
    private final WorldVector3i SK_OWNED2 = new WorldVector3i("second", 20, 0, -30);
    private final WorldVector3i VINCENT_OWNED = new WorldVector3i("main", 21, 0, -30);
    private final WorldVector3i VINCENT_OWNED2 = new WorldVector3i("second", 21, 0, -30);
    private final WorldVector3i UNOWNED = new WorldVector3i("main", 50, 0, -30);

    @Before
    public void setUp() throws Exception {
        db = MockDatabase.getInstance();
    }

    private DatabaseClaimMap createClaims(String server) {
        db.loadData();
        DataSource ds = db.createDataSource();
        MojangIdMapping idMapping = new DatabaseMojangIdMapping(ds);
        return new DatabaseClaimMap(ds, idMapping, server);
    }

    private void verifySKOwned(DatabaseClaimMap claims) {
        Claim claim = claims.get(SK_OWNED);
        assertThat(claim, notNullValue());
        assertThat(claim.getServer(), equalTo(MockDatabase.MOCK_SERVER));
        assertThat(claim.getPosition().getWorldId(), equalTo(SK_OWNED.getWorldId()));
        assertThat(claim.getOwner().getUuid(), equalTo(MockDatabase.SK_USER.getUuid()));
        assertThat(claim.getOwner().getName(), equalTo(MockDatabase.SK_USER.getName()));
        assertThat(claim.getParty(), equalTo("friends"));
        assertThat(claim.getPosition().getX(), is(SK_OWNED.getX()));
        assertThat(claim.getPosition().getZ(), is(SK_OWNED.getZ()));
        assertThat(claim.getIssueTime(), DateMatchers.sameInstant(MockDatabase.parseDate("2005-01-02 02:04:06")));
    }

    private void verifySKOwned2(DatabaseClaimMap claims) {
        Claim claim = claims.get(SK_OWNED2);
        assertThat(claim, notNullValue());
        assertThat(claim.getServer(), equalTo(MockDatabase.MOCK_SERVER));
        assertThat(claim.getPosition().getWorldId(), equalTo(SK_OWNED2.getWorldId()));
        assertThat(claim.getOwner().getUuid(), equalTo(MockDatabase.SK_USER.getUuid()));
        assertThat(claim.getOwner().getName(), equalTo(MockDatabase.SK_USER.getName()));
        assertThat(claim.getParty(), nullValue());
        assertThat(claim.getPosition().getX(), is(SK_OWNED2.getX()));
        assertThat(claim.getPosition().getZ(), is(SK_OWNED2.getZ()));
        assertThat(claim.getIssueTime(), DateMatchers.sameInstant(MockDatabase.parseDate("2005-01-03 00:00:00")));
    }

    private void verifyVincentOwned(DatabaseClaimMap claims) {
        Claim claim = claims.get(VINCENT_OWNED);
        assertThat(claim, notNullValue());
        assertThat(claim.getServer(), equalTo(MockDatabase.MOCK_SERVER));
        assertThat(claim.getPosition().getWorldId(), equalTo(VINCENT_OWNED.getWorldId()));
        assertThat(claim.getOwner().getUuid(), equalTo(MockDatabase.VINCENT_USER.getUuid()));
        assertThat(claim.getOwner().getName(), equalTo(MockDatabase.VINCENT_USER.getName()));
        assertThat(claim.getParty(), equalTo("guests"));
        assertThat(claim.getPosition().getX(), is(VINCENT_OWNED.getX()));
        assertThat(claim.getPosition().getZ(), is(VINCENT_OWNED.getZ()));
        assertThat(claim.getIssueTime(), DateMatchers.sameInstant(MockDatabase.parseDate("2005-02-03 00:10:00")));
    }

    private void verifyVincentOwned2(DatabaseClaimMap claims) {
        Claim claim = claims.get(VINCENT_OWNED2);
        assertThat(claim, notNullValue());
        assertThat(claim.getServer(), equalTo(MockDatabase.MOCK_SERVER));
        assertThat(claim.getPosition().getWorldId(), equalTo(VINCENT_OWNED2.getWorldId()));
        assertThat(claim.getOwner().getUuid(), equalTo(MockDatabase.VINCENT_USER.getUuid()));
        assertThat(claim.getOwner().getName(), equalTo(MockDatabase.VINCENT_USER.getName()));
        assertThat(claim.getParty(), equalTo("friends"));
        assertThat(claim.getPosition().getX(), is(VINCENT_OWNED2.getX()));
        assertThat(claim.getPosition().getZ(), is(VINCENT_OWNED2.getZ()));
        assertThat(claim.getIssueTime(), DateMatchers.sameInstant(MockDatabase.parseDate("2015-04-03 00:00:00")));
    }

    @Test
    public void testFindClaimByPosition_Missing() throws Exception {
        DatabaseClaimMap claims = createClaims(MockDatabase.MOCK_SERVER + "_MISSING");
        Claim claim;

        claim = claims.get(SK_OWNED);
        assertThat(claim, nullValue());
    }

    @Test
    public void testFindClaimByPosition() throws Exception {
        DatabaseClaimMap claims = createClaims(MockDatabase.MOCK_SERVER);
        Claim claim;

        claim = claims.get(SK_OWNED);
        assertThat(claim, notNullValue());
        assertThat(claim.getServer(), equalTo(MockDatabase.MOCK_SERVER));
        assertThat(claim.getPosition().getWorldId(), equalTo("main"));
        assertThat(claim.getOwner().getUuid(), equalTo(MockDatabase.SK_USER.getUuid()));
        assertThat(claim.getOwner().getName(), equalTo(MockDatabase.SK_USER.getName()));
        assertThat(claim.getParty(), equalTo("friends"));
        assertThat(claim.getPosition().getX(), is(SK_OWNED.getX()));
        assertThat(claim.getPosition().getZ(), is(SK_OWNED.getZ()));
        assertThat(claim.getIssueTime(), DateMatchers.sameInstant(MockDatabase.parseDate("2005-01-02 02:04:06")));
    }

    @Test
    public void testGetClaimCount() throws Exception {
        DatabaseClaimMap claims = createClaims(MockDatabase.MOCK_SERVER);
        assertThat(claims.getCountByOwner(MockDatabase.SK_USER), is(2));
        assertThat(claims.getCountByOwner(MockDatabase.VINCENT_USER), is(2));
    }

    @Test
    public void testSaveClaim() throws Exception {
        DatabaseClaimMap claims = createClaims(MockDatabase.MOCK_SERVER);
        Claim claim;

        claims.save(Lists.newArrayList(SK_OWNED, SK_OWNED2), MockDatabase.VINCENT_USER, "guests");
        List<Claim> returned = Lists.newArrayList(claims.getAll(Lists.newArrayList(SK_OWNED, SK_OWNED2)).values());

        assertThat(returned.size(), is(2));
        assertThat(returned, containsInAnyOrder(
                new Claim(MockDatabase.MOCK_SERVER, SK_OWNED),
                new Claim(MockDatabase.MOCK_SERVER, SK_OWNED2)));

        claim = claims.get(SK_OWNED);
        assertThat(claim, notNullValue());
        assertThat(claim.getServer(), equalTo(MockDatabase.MOCK_SERVER));
        assertThat(claim.getPosition().getWorldId(), equalTo(SK_OWNED.getWorldId()));
        assertThat(claim.getOwner().getUuid(), equalTo(MockDatabase.VINCENT_USER.getUuid()));
        assertThat(claim.getOwner().getName(), equalTo(MockDatabase.VINCENT_USER.getName()));
        assertThat(claim.getParty(), equalTo("guests"));
        assertThat(claim.getPosition().getX(), is(SK_OWNED.getX()));
        assertThat(claim.getPosition().getZ(), is(SK_OWNED.getZ()));

        claim = claims.get(SK_OWNED2);
        assertThat(claim, notNullValue());
        assertThat(claim.getServer(), equalTo(MockDatabase.MOCK_SERVER));
        assertThat(claim.getPosition().getWorldId(), equalTo(SK_OWNED2.getWorldId()));
        assertThat(claim.getOwner().getUuid(), equalTo(MockDatabase.VINCENT_USER.getUuid()));
        assertThat(claim.getOwner().getName(), equalTo(MockDatabase.VINCENT_USER.getName()));
        assertThat(claim.getParty(), equalTo("guests"));
        assertThat(claim.getPosition().getX(), is(SK_OWNED2.getX()));
        assertThat(claim.getPosition().getZ(), is(SK_OWNED2.getZ()));

        verifyVincentOwned(claims);
        verifyVincentOwned2(claims);
    }

    @Test
    public void testUpdateClaim_ExistingUser() throws Exception {
        DatabaseClaimMap claims = createClaims(MockDatabase.MOCK_SERVER);
        Claim claim;

        claims.update(Lists.newArrayList(SK_OWNED, VINCENT_OWNED, UNOWNED), MockDatabase.VINCENT_USER, "guests", MockDatabase.VINCENT_USER);

        verifySKOwned(claims);

        claim = claims.get(VINCENT_OWNED);
        assertThat(claim, notNullValue());
        assertThat(claim.getServer(), equalTo(MockDatabase.MOCK_SERVER));
        assertThat(claim.getPosition().getWorldId(), equalTo(VINCENT_OWNED.getWorldId()));
        assertThat(claim.getOwner().getUuid(), equalTo(MockDatabase.VINCENT_USER.getUuid()));
        assertThat(claim.getOwner().getName(), equalTo(MockDatabase.VINCENT_USER.getName()));
        assertThat(claim.getParty(), equalTo("guests"));
        assertThat(claim.getPosition().getX(), is(VINCENT_OWNED.getX()));
        assertThat(claim.getPosition().getZ(), is(VINCENT_OWNED.getZ()));

        verifyVincentOwned2(claims);

        claim = claims.get(UNOWNED);
        assertThat(claim, notNullValue());
        assertThat(claim.getServer(), equalTo(MockDatabase.MOCK_SERVER));
        assertThat(claim.getPosition().getWorldId(), equalTo(UNOWNED.getWorldId()));
        assertThat(claim.getOwner().getUuid(), equalTo(MockDatabase.VINCENT_USER.getUuid()));
        assertThat(claim.getOwner().getName(), equalTo(MockDatabase.VINCENT_USER.getName()));
        assertThat(claim.getParty(), equalTo("guests"));
        assertThat(claim.getPosition().getX(), is(UNOWNED.getX()));
        assertThat(claim.getPosition().getZ(), is(UNOWNED.getZ()));
    }

    @Test
    public void testUpdateClaim_Unowned() throws Exception {
        DatabaseClaimMap claims = createClaims(MockDatabase.MOCK_SERVER);
        Claim claim;

        claims.update(Lists.newArrayList(SK_OWNED, VINCENT_OWNED, UNOWNED), MockDatabase.VINCENT_USER, "guests", null);

        verifySKOwned(claims);
        verifyVincentOwned(claims);
        verifyVincentOwned2(claims);

        claim = claims.get(UNOWNED);
        assertThat(claim, notNullValue());
        assertThat(claim.getServer(), equalTo(MockDatabase.MOCK_SERVER));
        assertThat(claim.getPosition().getWorldId(), equalTo(UNOWNED.getWorldId()));
        assertThat(claim.getOwner().getUuid(), equalTo(MockDatabase.VINCENT_USER.getUuid()));
        assertThat(claim.getOwner().getName(), equalTo(MockDatabase.VINCENT_USER.getName()));
        assertThat(claim.getParty(), equalTo("guests"));
        assertThat(claim.getPosition().getX(), is(UNOWNED.getX()));
        assertThat(claim.getPosition().getZ(), is(UNOWNED.getZ()));
    }

}
