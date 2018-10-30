/*
 *  Copyright (c) 2014-2017 Kumuluz and/or its affiliates
 *  and other contributors as indicated by the @author tags and
 *  the contributor list.
 *
 *  Licensed under the MIT License (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://opensource.org/licenses/MIT
 *
 *  The software is provided "AS IS", WITHOUT WARRANTY OF ANY KIND, express or
 *  implied, including but not limited to the warranties of merchantability,
 *  fitness for a particular purpose and noninfringement. in no event shall the
 *  authors or copyright holders be liable for any claim, damages or other
 *  liability, whether in an action of contract, tort or otherwise, arising from,
 *  out of or in connection with the software or the use or other dealings in the
 *  software. See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.kumuluz.ee.testing.arquillian.utils;

import com.kumuluz.ee.testing.arquillian.assets.MainWrapper;
import com.kumuluz.ee.testing.arquillian.assets.ServletWebListener;
import org.jboss.shrinkwrap.api.*;
import org.jboss.shrinkwrap.api.asset.*;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.base.asset.AssetUtil;
import org.jboss.shrinkwrap.impl.base.path.BasicPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Contains utilities for processing ShrinkWrap archives.
 *
 * @author Urban Malc
 * @since 1.0.0
 */
public class ArchiveUtils {

    private static final Logger LOG = Logger.getLogger(ArchiveUtils.class.getName());

    /**
     * These classes are added to the root of the archive
     */
    private static final Class<?>[] rootClasses = {
            MainWrapper.class,
            ServletWebListener.class
    };

    private static JavaArchive generateWar(Archive<?> archive, List<String> deploymentLibraries) {
        JavaArchive javaArchive = archive.as(JavaArchive.class);

        Archive<?>[] requiredLibraries = RequiredLibraries.getRequiredLibraries(deploymentLibraries);

        Arrays.stream(requiredLibraries).forEach(f -> javaArchive.add(new ArchiveAsset(f, ZipExporter.class),
                "/WEB-INF/lib/" + f.getName()));

        return javaArchive;
    }

    public static JavaArchive generateUberJar(Archive<?> archive) {

        // start from war
        JavaArchive javaArchive = generateWar(archive, Collections.singletonList("com.kumuluz.ee:kumuluzee-loader:"));

        // explode application archive and kumuluzee-loader lib into root
        explodeAppArchiveToRoot(javaArchive);
        explodeLoaderArchiveToRoot(javaArchive);

        // apply necessary transformations from web archive to java archive
        moveDir(javaArchive, "/WEB-INF/classes", "");
        moveDir(javaArchive, "/WEB-INF/lib", "/lib");
        // CDI enricher adds beans.xml to the WEB-INF directory, if supplied archive is WebArchive.
        // If that is the case, we need to move it to the META-INF directory.
        if (javaArchive.contains("/WEB-INF/beans.xml") && !javaArchive.contains("/META-INF/beans.xml")) {
            javaArchive.move("/WEB-INF/beans.xml", "/META-INF/beans.xml");
        }

        // add required root classes to classes dir
        for (Class<?> klass : rootClasses) {
            javaArchive.addClass(klass); // will get added to root, which is correct
        }

        javaArchive.addAsManifestResource("KumuluzEEManifest.MF", "MANIFEST.MF");
        javaArchive.addAsManifestResource(
                new StringAsset("main-class=" + MainWrapper.class.getName()),
                "kumuluzee/boot-loader.properties");

        return javaArchive;
    }

    public static JavaArchive generateExploded(Archive<?> archive) {
        // start from war
        JavaArchive javaArchive = generateWar(archive, Collections.emptyList());
        // explode application archive into root
        explodeAppArchiveToRoot(javaArchive);
        // convert archive assets to byte array assets
        fixArchiveAssets(javaArchive, "/WEB-INF/lib");

        // apply necessary transformations from web archive to exploded form
        // move root directories (except WEB-INF) to /classes
        List<String> dirsToMove = new ArrayList<>();
        for (Node n : javaArchive.get("/").getChildren()) {
            if (n.getAsset() == null && !n.getPath().get().endsWith("WEB-INF")) {
                dirsToMove.add(n.getPath().get());
            }
        }
        for (String path : dirsToMove) {
            moveDir(javaArchive, path, "/classes" + path);
        }
        // move contents of WEB-INF
        moveDir(javaArchive, "/WEB-INF/classes", "/classes");
        moveDir(javaArchive, "/WEB-INF/lib", "/dependency");

        // CDI enricher adds beans.xml to the WEB-INF directory, if supplied archive is WebArchive.
        // If that is the case, we need to move it to the META-INF directory.
        if (javaArchive.contains("/WEB-INF/beans.xml") && !javaArchive.contains("/classes/META-INF/beans.xml")) {
            javaArchive.move("/WEB-INF/beans.xml", "/classes/META-INF/beans.xml");
        }

        // move the rest of the WEB-INF files to /classes/webapp/WEB-INF
        javaArchive.addAsDirectory("/classes/webapp");
        moveDir(javaArchive, "/WEB-INF", "/classes/webapp/WEB-INF");

        // add required root classes to classes dir
        for (Class<?> klass : rootClasses) {
            Asset classAsset = new ClassAsset(klass);
            ArchivePath classArchivePath = new BasicPath("/classes",
                    AssetUtil.getFullPathForClassResource(klass));
            javaArchive.add(classAsset, classArchivePath);
        }

        return javaArchive;
    }

    /**
     * Explodes archive marked with {@link ApplicationArchiveMarker} to root of the archive.
     *
     * @param javaArchive Archive with all jars in /WEB-INF/lib (war)
     */
    private static void explodeAppArchiveToRoot(JavaArchive javaArchive) {
        for (Node n : javaArchive.get("/WEB-INF/lib").getChildren()) {
            if (n.getAsset() instanceof ArchiveAsset) {
                Archive<?> dependencyJar = ((ArchiveAsset) n.getAsset()).getArchive();
                if (dependencyJar.contains(ApplicationArchiveMarker.MARKER_FILENAME)) {
                    LOG.fine("Found application archive: " + dependencyJar.getName());
                    dependencyJar.delete(ApplicationArchiveMarker.MARKER_FILENAME);
                    javaArchive.merge(dependencyJar);
                    javaArchive.delete(n.getPath());
                    break;
                }
            }
        }
    }

    /**
     * Explodes kumuluzee-loader library to root of the archive.
     *
     * @param javaArchive Archive with all jars in /WEB-INF/lib (war)
     */
    private static void explodeLoaderArchiveToRoot(JavaArchive javaArchive) {
        for (Node n : javaArchive.get("/WEB-INF/lib").getChildren()) {
            if (n.getAsset() instanceof ArchiveAsset &&
                    ((ArchiveAsset) n.getAsset()).getArchive().getName().startsWith("kumuluzee-loader-")) {
                Archive<?> dependencyJar = ((ArchiveAsset) n.getAsset()).getArchive();
                LOG.fine("Found kumuluzee-loader archive: " + dependencyJar.getName());
                javaArchive.merge(dependencyJar);
                javaArchive.delete(n.getPath());
                break;
            }
        }
    }

    /**
     * Converts {@link ArchiveAsset}s in parentDir to {@link ByteArrayAsset}s.
     * <p>
     * By default {@link org.jboss.shrinkwrap.api.exporter.ExplodedExporter} exports {@link ArchiveAsset}s in parent
     * archive as exploded directories. Since we need to export them as archives, we convert them to
     * {@link ByteArrayAsset}s.
     *
     * @param a         Archive to fix
     * @param parentDir Directory to scan for {@link ArchiveAsset}s
     */
    private static void fixArchiveAssets(Archive<?> a, String parentDir) {
        Node n = a.get(parentDir);
        Archive<?> tmp = ShrinkWrap.create(JavaArchive.class);
        List<ArchivePath> pathsToDelete = new ArrayList<>();
        for (Node child : n.getChildren()) {
            Asset childAsset = child.getAsset();
            if (childAsset instanceof ArchiveAsset && child.getPath().get().endsWith(".jar")) {
                LOG.fine("Converting archive " + child.getPath().get() + " to ByteArrayAsset");
                ArchiveAsset archiveAsset = (ArchiveAsset) childAsset;
                ByteArrayAsset bas = new ByteArrayAsset(archiveAsset.openStream());
                pathsToDelete.add(child.getPath());
                tmp.add(bas, child.getPath());
            }
        }

        for (ArchivePath ap : pathsToDelete) {
            a.delete(ap);
        }
        a.merge(tmp);
    }

    private static void moveDir(Archive<?> archive, String source, String target) {
        if (archive.contains(source)) {
            Archive<?> tmp = ShrinkWrap.create(JavaArchive.class);

            copyDir(archive, tmp, source, target);

            archive.merge(tmp);
            archive.delete(source);
        }
    }

    private static void copyDir(Archive<?> sourceArchive, Archive<?> targetArchive, String source, String target) {
        Node sourceNode = sourceArchive.get(source);

        if (sourceNode.getAsset() != null) {
            targetArchive.add(sourceNode.getAsset(), target);
        } else {
            for (Node child : sourceNode.getChildren()) {
                String childName = child.getPath().get().replaceFirst(child.getPath().getParent().get(), "");
                copyDir(sourceArchive, targetArchive,
                        child.getPath().get(), ArchivePaths.create(target, childName).get());
            }
        }
    }
}
