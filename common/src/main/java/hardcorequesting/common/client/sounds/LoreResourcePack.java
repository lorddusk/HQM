package hardcorequesting.common.client.sounds;

public class LoreResourcePack {}
/*
public class LoreResourcePack extends AbstractFileResourcePack {
    
    private static final Set domains = Sets.newHashSet("hardcorequesting");
    
    public LoreResourcePack(File folder) {
        super(folder);
    }
    
    @Override
    public InputStream getInputStream(Identifier resource) throws IOException {
        return this.getInputStreamByName(resource.getPath().replace("sounds/", ""));
    }
    
    @Override
    public boolean resourceExists(Identifier resource) {
        return containsFile(resource.getPath().replace("sounds/", ""));
    }
    
    @Override
    protected InputStream getInputStreamByName(String name) throws IOException {
        return new BufferedInputStream(new FileInputStream(new File(this.base, "lore.ogg")));
    }
    
    @Override
    protected boolean containsFile(String name) {
        return name.contains("lore") && name.endsWith(".ogg") && new File(this.base, "lore.ogg").isFile();
    }
    
    @Override
    public Set getResourceDomains() {
        return domains;
    }
}

 */