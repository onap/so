package org.openecomp.mso.db.catalog.data.repository;

import org.openecomp.mso.db.catalog.beans.ArRecipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "arRecipe", path = "arRecipe")
public interface ArRecipeRepository extends JpaRepository<ArRecipe, String> {

	public ArRecipe findByModelNameAndAction(String modelName, String action);
}