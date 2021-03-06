/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.openshift.internal.restclient.capability.resources;

import org.apache.commons.lang.StringUtils;

import com.openshift.restclient.IClient;
import com.openshift.restclient.capability.resources.IPortForwardable;
import com.openshift.restclient.model.IPod;

/**
 * Port forwarding implementation that wraps the OpenShift binary
 * 
 * @author jeff.cantrill
 *
 */
public class OpenShiftBinaryPortForwarding extends AbstractOpenShiftBinaryCapability implements IPortForwardable {
	
	
	private IPod pod;
	private PortPair[] pairs = new PortPair[] {};

	public OpenShiftBinaryPortForwarding(IPod pod, IClient client) {
		super(client);
		this.pod = pod;
	}
	
	
	@Override
	protected void cleanup() {
		this.pairs = new PortPair[] {};
	}

	@Override
	protected boolean validate() {
		return pairs.length != 0;
	}


	@Override
	public boolean isForwarding() {
		return getProcess() != null && getProcess().isAlive();
	}

	@Override
	public boolean isSupported() {
		return true;
	}

	@Override
	public String getName() {
		return OpenShiftBinaryPortForwarding.class.getSimpleName();
	}

	@Override
	public PortPair[] getPortPairs() {
		return pairs;
	}

	@Override
	public synchronized void forwardPorts(PortPair... ports) {
		this.pairs = ports;
		start();
	}
	
	@Override
	protected String[] buildArgs(String location) {
		StringBuilder args = new StringBuilder(location);
		args.append(" port-forward ")
			.append("--insecure-skip-tls-verify=true ")
			.append("--server=").append(getClient().getBaseURL()).append(" ");
			addToken(args)
			.append("-n ").append(pod.getNamespace()).append(" ")
			.append("-p ").append(pod.getName()).append(" ");
		for (PortPair pair : pairs) {
			args.append(pair.getLocalPort()).append(":").append(pair.getRemotePort()).append(" ");
		}
		return StringUtils.split(args.toString(), " ");
	}
	
}
