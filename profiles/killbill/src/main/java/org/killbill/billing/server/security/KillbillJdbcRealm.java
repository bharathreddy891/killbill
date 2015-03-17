/*
 * Copyright 2010-2013 Ning, Inc.
 * Copyright 2014-2015 Groupon, Inc
 * Copyright 2014-2015 The Billing Project, LLC
 *
 * The Billing Project licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.killbill.billing.server.security;

import javax.sql.DataSource;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.util.ByteSource;
import org.killbill.billing.tenant.security.KillbillCredentialsMatcher;

/**
 * @see {shiro.ini}
 */
public class KillbillJdbcRealm extends JdbcRealm {

    private static final String KILLBILL_AUTHENTICATION_QUERY = "select api_secret, api_salt from tenants where api_key = ?";

    public KillbillJdbcRealm(final DataSource dataSource) {
        super();

        setDataSource(dataSource);

        configureSecurity();
        configureQueries();
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken token) throws AuthenticationException {
        final SimpleAuthenticationInfo authenticationInfo = (SimpleAuthenticationInfo) super.doGetAuthenticationInfo(token);

        // We store the salt bytes in Base64 (because the JdbcRealm retrieves it as a String)
        final ByteSource base64Salt = authenticationInfo.getCredentialsSalt();
        authenticationInfo.setCredentialsSalt(ByteSource.Util.bytes(Base64.decode(base64Salt.getBytes())));

        return authenticationInfo;
    }

    private void configureSecurity() {
        setSaltStyle(SaltStyle.COLUMN);
        setCredentialsMatcher(KillbillCredentialsMatcher.getCredentialsMatcher());
    }

    private void configureQueries() {
        setAuthenticationQuery(KILLBILL_AUTHENTICATION_QUERY);
    }
}
