USE build_release/dist_fxbuild.xml to perform the distribution build. The target is do-custom-dist.

To refresh or recreate the build.xml, 

1. create build.xml using build.fxbuild (just creating without running)
2. move build.xml to fxbuild folder
3. comment out the section of compiling resource folder into the jar
		<!-- NZ 2016-04-01 put resources external -->
		<!--<copy todir="build/src">
			<fileset dir="project/resource">
				<include name="**/*"/>
			</fileset>
		</copy>
		-->
4. make sure build_release/dist_fxbuild.xml is there with basedir="../fxbuild" (without this, the build process will delete some project files!!!)