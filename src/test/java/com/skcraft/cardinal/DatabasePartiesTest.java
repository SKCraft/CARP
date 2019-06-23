package com.skcraft.cardinal;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.skcraft.cardinal.profile.DatabaseMojangIdMapping;
import com.skcraft.cardinal.profile.MojangId;
import com.skcraft.cardinal.profile.MojangIdMapping;
import com.skcraft.cardinal.service.party.DatabasePartyManager;
import com.skcraft.cardinal.service.party.Member;
import com.skcraft.cardinal.service.party.Party;
import com.skcraft.cardinal.service.party.PartyExistsException;
import com.skcraft.cardinal.service.party.Rank;
import org.junit.Before;
import org.junit.Test;
import uk.co.it.modular.hamcrest.date.DateMatchers;

import javax.sql.DataSource;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DatabasePartiesTest {

    private static final MojangId LEISER_USER = new MojangId(UUID.fromString("12345678-4321-1945-1111-223344556677"), "LeiserGeist");

    private MockDatabase db;

    @Before
    public void setUp() throws Exception {
        db = MockDatabase.getInstance();
    }

    private DatabasePartyManager createParties() {
        db.loadData();
        DataSource ds = db.createDataSource();
        MojangIdMapping idMapping = new DatabaseMojangIdMapping(ds);
        return new DatabasePartyManager(ds, idMapping);
    }

    @Test
    public void testCreate_New() throws Exception {
        DatabasePartyManager partyManager = createParties();

        Party party = new Party();
        party.setName("noobs");
        party.setMembers(Sets.newHashSet(new Member(LEISER_USER, Rank.MEMBER), new Member(MockDatabase.VINCENT_USER, Rank.OWNER), new Member(MockDatabase.SK_USER, Rank.MEMBER)));
        party.setCreateTime(MockDatabase.parseDate("2012-01-02 11:12:13"));
        partyManager.create(party);

        Party addedParty = partyManager.get("noobs");
        assertThat(party.getName(), equalTo("noobs"));
        assertThat(party.getCreateTime(), equalTo(MockDatabase.parseDate("2012-01-02 11:12:13")));

        assertThat(party.getMembers().size(), is(3));
        assertThat(party.getMembers(), containsInAnyOrder(new Member(MockDatabase.VINCENT_USER, Rank.OWNER), new Member(LEISER_USER, Rank.MEMBER), new Member(MockDatabase.SK_USER, Rank.MEMBER)));
    }

    @Test(expected = PartyExistsException.class)
    public void testCreate_Existing() throws Exception {
        DatabasePartyManager partyManager = createParties();

        Party party = new Party();
        party.setName("friends");
        party.setMembers(Sets.newHashSet(new Member(MockDatabase.VINCENT_USER, Rank.OWNER), new Member(MockDatabase.SK_USER, Rank.MEMBER)));
        party.setCreateTime(MockDatabase.parseDate("2015-03-05 10:20:30"));

        partyManager.create(party);
    }

    @Test
    public void testFindPartiesByName() throws Exception {
        DatabasePartyManager partyManager = createParties();
        Map<String, Party> parties = partyManager.getAll(Lists.newArrayList("guests", "friends"));

        assertThat(parties.size(), is(2));
        assertThat(parties.get("guests").getCreateTime(), DateMatchers.sameSecond(MockDatabase.parseDate("2015-03-05 10:20:30")));
        assertThat(parties.get("friends").getCreateTime(), DateMatchers.sameSecond(MockDatabase.parseDate("2015-02-04 10:20:30")));
    }

    @Test
    public void testget() throws Exception {
        DatabasePartyManager partyManager = createParties();
        Party party = partyManager.get("guests");

        assertThat(party.getName(), equalTo("guEsTs"));
        assertThat(party.getCreateTime(), DateMatchers.sameSecond(MockDatabase.parseDate("2015-03-05 10:20:30")));

        assertThat(party.getMembers().size(), is(2));
        assertThat(party.getMembers(), containsInAnyOrder(new Member(MockDatabase.VINCENT_USER, Rank.OWNER), new Member(MockDatabase.SK_USER, Rank.MEMBER)));
    }

    @Test
    public void testRefreshParty() throws Exception {
        DatabasePartyManager partyManager = createParties();
        Party party = partyManager.get("guests");

        assertThat(partyManager.refresh(party), is(false));

        assertThat(partyManager.get("guests").getCreateTime(), DateMatchers.sameSecond(MockDatabase.parseDate("2015-03-05 10:20:30")));
    }

    @Test
    public void testAddMembers() throws Exception {
        DatabasePartyManager partyManager = createParties();
        Party party = partyManager.get("friends");

        assertThat(party.getMembers().size(), is(1));

        partyManager.addMembers("friends", Sets.newHashSet(new Member(MockDatabase.VINCENT_USER, Rank.MEMBER)));
        partyManager.refresh(party);

        assertThat(party.getMembers().size(), is(2));
        assertThat(party.getMembers(), containsInAnyOrder(
                new Member(MockDatabase.VINCENT_USER, Rank.OWNER),
                new Member(MockDatabase.SK_USER, Rank.MEMBER)
        ));
    }

    @Test
    public void testRemoveMembers() throws Exception {
        DatabasePartyManager partyManager = createParties();
        Party party = partyManager.get("guests");

        assertThat(party.getMembers().size(), is(2));

        partyManager.removeMembers("guests", Sets.newHashSet(MockDatabase.SK_USER));
        partyManager.refresh(party);

        assertThat(party.getMembers().size(), is(1));

    }
}
