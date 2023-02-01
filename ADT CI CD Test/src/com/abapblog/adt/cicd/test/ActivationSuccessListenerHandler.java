package com.abapblog.adt.cicd.test;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import com.sap.adt.activation.ui.IActivationSuccessListener;
import com.sap.adt.atc.AtcBackendServices;
import com.sap.adt.atc.AtcRunOptions;
import com.sap.adt.atc.AtcRunnerFactory;
import com.sap.adt.atc.IAtcCheckableItem;
import com.sap.adt.atc.IAtcResult;
import com.sap.adt.atc.IAtcRunner;
import com.sap.adt.atc.IAtcWorklistBackendAccess;
import com.sap.adt.atc.model.atcworklist.IAtcWorklist;
import com.sap.adt.atc.model.atcworklist.IAtcWorklistRun;
import com.sap.adt.communication.exceptions.CommunicationException;
import com.sap.adt.communication.resources.ResourceException;
import com.sap.adt.compatibility.exceptions.OutDatedClientException;
import com.sap.adt.tools.abapsource.common.IRestResourceFactoryFacade;
import com.sap.adt.tools.abapsource.common.RestResourceFactoryFacade;
import com.sap.adt.tools.core.IAdtObjectReference;
import com.sap.adt.tools.core.internal.AbapProjectService;
import com.sap.adt.tools.core.project.IAbapProject;

@SuppressWarnings("restriction")
public class ActivationSuccessListenerHandler implements IActivationSuccessListener {

	@Override
	public void onActivationSuccess(List<IAdtObjectReference> adtObject, IProject project) {
		String packageName = "$ASE_DRYNDAR"; // change it here
		String atcVariantName = "ZHAGER_DEFAULT"; // change it here
		AndreasGautschWayOfAtcCall(project, packageName, atcVariantName);
		secondWayOfAtcCall(project, packageName, atcVariantName);
//after refresh of ATC Result browser view, both runs should be visible there.
	}

	private void AndreasGautschWayOfAtcCall(IProject project, String packageName, String atcVariantName) {
		IAtcWorklistBackendAccess worklistBackendAccess = AtcBackendServices.getWorklistBackendAccess();
		List<IAtcCheckableItem> checkableItems = new ArrayList<>();
		IProgressMonitor progressMonitor = new NullProgressMonitor();
		IAbapProject abapProject = AbapProjectService.getInstance().createFromProjectUnchecked(project);
		String worklistId = worklistBackendAccess.createWorklist(abapProject, atcVariantName, progressMonitor);
		checkableItems.add(new _AtcCheckableItem(
				URI.create("/sap/bc/adt/vit/wb/object_type/devck/object_name/" + packageName), packageName, "DEVC/K"));
		IAtcWorklistRun worklistRun = worklistBackendAccess.startAtcRunForWorklist(abapProject, checkableItems,
				worklistId, false, progressMonitor);
		boolean forceObjectSet = true;
		boolean includeExemptedFindings = false;

		try {
			IAtcWorklist worklist = worklistBackendAccess.getWorklist(abapProject, worklistRun.getWorklistId(),
					worklistRun.getWorklistTimestamp().toString(), packageName, forceObjectSet, includeExemptedFindings,
					progressMonitor, "/sap/bc/adt/atc/worklists/" + worklistRun.getWorklistId());
			// then he iterate over worklist objects and so on.
			// more can be found here
			// https://github.com/andau/abapCI/blob/e91674bd0c108e33e248f12c3534e7b0f3a44db9/eclipsePlugin/abapCI/src/abapci/handlers/AbapAtcHandler.java

		} catch (CommunicationException | ResourceException | OutDatedClientException | OperationCanceledException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}

	private void secondWayOfAtcCall(IProject project, String packageName, String atcVariantName) {
		// this is probably more dirty way, only synchronous / without using working
		// sets
		// atc results are collected but, there is no way to display them
		// as AtcDisplayController is in restricted package
		IRestResourceFactoryFacade rff = new RestResourceFactoryFacade();
		IAtcRunner atcRunner = AtcRunnerFactory.createAtcRunner(rff, project.getName());
		Set<URI> uri = new HashSet();
		uri.add(URI.create("/sap/bc/adt/vit/wb/object_type/devck/object_name/" + packageName));
		AtcRunOptions atcOptions = new AtcRunOptions();
		atcOptions.setCheckVariantName(atcVariantName); // possibility
		IAtcResult results = atcRunner.checkItemsAtBackend(uri, atcOptions);
		// As AtcDisplayController is in internal package, then it cannot be used in
		// plugins
//		AtcDisplayController atcDisplayController = AtcDisplayController.getInstance();
//		try {
//			atcDisplayController.showResult(project, results);
//		} catch (CoreException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	private class _AtcCheckableItem implements IAtcCheckableItem {

		private final URI uri;
		private final String name;
		private final String type;

		public _AtcCheckableItem(URI uri, String name, String type) {
			this.uri = uri;
			this.name = name;
			this.type = type;
		}

		@Override
		public <T> T getAdapter(Class<T> adapter) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public URI getUri() {
			return uri;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public int hashCode() {
			int result = 1;
			result = 31 * result + (name != null ? name.hashCode() : 0);
			result = 31 * result + (uri != null ? uri.hashCode() : 0);
			result = 31 * result + (type != null ? type.hashCode() : 0);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			_AtcCheckableItem other = (_AtcCheckableItem) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (uri == null) {
				if (other.uri != null)
					return false;
			} else if (!uri.equals(other.uri))
				return false;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		}

		public String getType() {
			return type;
		}

	}

}
