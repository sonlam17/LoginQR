package com.secsign.model.jpa.entity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
/** */
@NamedQueries({
        @NamedQuery(
                name = "getQrEntityByRealmIdAndId",
                query = "SELECT o FROM QrEntity o WHERE o.realmId = :realmId AND o.id = :id"),
        @NamedQuery(
                name = "getQrEntityByRealmId",
                query = "SELECT o FROM QrEntity o WHERE o.realmId = :realmId"),
        @NamedQuery(
                name = "getOrganizationCount",
                query = "select count(o) from QrEntity o where o.realmId = :realmId"),
        @NamedQuery(
                name = "removeAllQrEntity",
                query = "delete from QrEntity o where o.realmId = :realmId")
})
@Entity
@Getter
@Setter
@Table(name = "QR")
public class QrEntity {
    @Id
    @Column(name = "ID", length = 36)
    @Access(
            AccessType.PROPERTY) // we do this because relationships often fetch id, but not entity.  This
    // avoids an extra SQL
    protected String id;

    @Column(name = "REALM_ID", nullable = false)
    protected String realmId;

    @Column(name = "CONTENT", nullable = false)
    protected String content;

    @Column(name = "STATE", nullable = false)
    protected String state;
}
