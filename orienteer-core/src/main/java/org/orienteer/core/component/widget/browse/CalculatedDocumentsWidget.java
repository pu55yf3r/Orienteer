package org.orienteer.core.component.widget.browse;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;
import org.orienteer.core.component.widget.AbstractCalculatedDocumentsWidget;
import org.orienteer.core.widget.Widget;
import ru.ydn.wicket.wicketorientdb.model.OQueryDataProvider;

/**
 * Widget for calculated document
 */
@Widget(id="browse-calculated-documents", domain="browse", order=20, oClass = AbstractCalculatedDocumentsWidget.WIDGET_OCLASS_NAME, autoEnable=false)
public class CalculatedDocumentsWidget extends AbstractCalculatedDocumentsWidget<OClass> {

	public CalculatedDocumentsWidget(String id, IModel<OClass> model, IModel<ODocument> widgetDocumentModel) {
		super(id, model, widgetDocumentModel);
	}

	@Override
	protected OQueryDataProvider<ODocument> newDataProvider(String sql) {
		OQueryDataProvider<ODocument> provider = new OQueryDataProvider<ODocument>(sql);
		return provider;
	}

	@Override
	protected String getSql() {
		String sql = super.getSql();
		if(!Strings.isEmpty(sql)) return sql;
		else {
			String requiredClass = getWidgetDocument().field("class");
			return "select from "+(Strings.isEmpty(requiredClass)?getModelObject().getName():requiredClass);
		}
	}
}