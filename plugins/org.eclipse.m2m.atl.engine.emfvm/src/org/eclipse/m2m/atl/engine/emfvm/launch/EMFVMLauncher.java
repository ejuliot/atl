package org.eclipse.m2m.atl.engine.emfvm.launch;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2m.atl.common.ATLLogger;
import org.eclipse.m2m.atl.core.IModel;
import org.eclipse.m2m.atl.core.launch.ILauncher;
import org.eclipse.m2m.atl.engine.emfvm.ASM;
import org.eclipse.m2m.atl.engine.emfvm.ASMXMLReader;
import org.eclipse.m2m.atl.engine.emfvm.Messages;

/**
 * The EMFVM implementation of the {@link ILauncher} interface.
 * 
 * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
 * @author <a href="mailto:dennis.wagelaar@vub.ac.be">Dennis Wagelaar</a>
 */
public class EMFVMLauncher implements ILauncher {

	private static final String LAUNCHER_NAME = "EMF-specific VM"; //$NON-NLS-1$

	private static final String MODEL_FACTORY_NAME = "EMF"; //$NON-NLS-1$

	private Map<String, IModel> models;

	private Map<String, ASM> libraries;

	public String getName() {
		return LAUNCHER_NAME;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.m2m.atl.core.launch.ILauncher#addInModel(org.eclipse.m2m.atl.core.IModel,
	 *      java.lang.String, java.lang.String)
	 */
	public void addInModel(IModel model, String name, String referenceModelName) {
		if (models.containsKey(name)) {
			ATLLogger.warning(Messages.getString("EMFVMLauncher.MODEL_EVER_REGISTERED", name)); //$NON-NLS-1$
		} else {
			models.put(name, model);
		}
		if (!models.containsKey(referenceModelName)) {
			models.put(referenceModelName, model.getReferenceModel());
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.m2m.atl.core.launch.ILauncher#addInOutModel(org.eclipse.m2m.atl.core.IModel,
	 *      java.lang.String, java.lang.String)
	 */
	public void addInOutModel(IModel model, String name, String referenceModelName) {
		model.setIsTarget(true);
		addInModel(model, name, referenceModelName);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.m2m.atl.core.launch.ILauncher#addOutModel(org.eclipse.m2m.atl.core.IModel,
	 *      java.lang.String, java.lang.String)
	 */
	public void addOutModel(IModel model, String name, String referenceModelName) {
		model.setIsTarget(true);
		addInModel(model, name, referenceModelName);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.m2m.atl.core.launch.ILauncher#addLibrary(java.lang.String, java.io.InputStream)
	 */
	public void addLibrary(String name, InputStream library) {
		if (libraries.containsKey(name)) {
			ATLLogger.warning(Messages.getString("EMFVMLauncher.LIBRARY_EVER_REGISTERED", name)); //$NON-NLS-1$
		} else {
			ASMXMLReader reader = new ASMXMLReader();
			ASM asmLibrary = reader.read(library);
			libraries.put(name, asmLibrary);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.m2m.atl.core.launch.ILauncher#initialize(java.util.Map)
	 */
	public void initialize(Map<String, Object> parameters) {
		models = new HashMap<String, IModel>();
		libraries = new HashMap<String, ASM>();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.m2m.atl.core.launch.ILauncher#launch(java.lang.String,
	 *      org.eclipse.core.runtime.IProgressMonitor, java.util.Map, java.io.InputStream[])
	 */
	public Object launch(final String mode, final IProgressMonitor monitor,
			final Map<String, Object> options, final InputStream... modules) {
		if (!mode.equals(ILauncher.RUN_MODE)) {
			ATLLogger.warning("mode " + mode + " unsupported for EMFVM, running launch instead"); //$NON-NLS-1$//$NON-NLS-2$
		}
		List<ASM> superimpose = new ArrayList<ASM>();
		ASMXMLReader reader = new ASMXMLReader();
		ASM mainModule = reader.read(modules[0]);
		for (int i = 1; i < modules.length; i++) {
			reader = new ASMXMLReader();
			InputStream module = modules[i];
			superimpose.add(reader.read(module));
		}
		return mainModule.run(models, libraries, superimpose, options, monitor);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.m2m.atl.core.launch.ILauncher#getModel(java.lang.String)
	 */
	public IModel getModel(String modelName) {
		return models.get(modelName);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.m2m.atl.core.launch.ILauncher#getLibrary(java.lang.String)
	 */
	public Object getLibrary(String libraryName) {
		return libraries.get(libraryName);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.m2m.atl.core.launch.ILauncher#getDefaultModelFactoryName()
	 */
	public String getDefaultModelFactoryName() {
		return MODEL_FACTORY_NAME;
	}

}