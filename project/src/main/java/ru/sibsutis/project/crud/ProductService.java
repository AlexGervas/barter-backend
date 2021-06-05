package ru.sibsutis.project.crud;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import ru.sibsutis.project.NotFoundException;
import ru.sibsutis.project.SearchPath;
import ru.sibsutis.project.Vertex;
import ru.sibsutis.project.databases.Product;
import ru.sibsutis.project.databases.User;
import ru.sibsutis.project.dto.ProductDto;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public List<Product> getAll() {
        return repository.findAll();
    }

    public Product create(ProductDto productDto, Long userID) {
        Product product = new Product();
        BeanUtils.copyProperties(productDto, product, "name");
        product.setName(productDto.getName().toLowerCase());
        User owner = userRepository.findById(userID).orElseThrow(NotFoundException::new);
        product.setOwner(owner);
        product.setStatus(true);//В КОНЕЧНОЙ ВЕРСИИ БУДЕТ ИЗНАЧАЛЬНО false. На true меняется при поступлении товара в офис
        return repository.save(product);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public List<Product> getById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(NotFoundException::new);
        return repository.findByOwner(user);
    }

    public List<Product> getFavesById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(NotFoundException::new);
        return user.getFavorites();
    }

    public List<Product> getExchangesByProductId(Long productId) {
        return repository.findById(productId).orElseThrow(NotFoundException::new).getProductsForExchange();
    }

    public List<Product> getProductsByCategory(String category) {
        return repository.findByCategory(category);
    }

    public void addFromProfile(Long productId, List<Product> products) {
        Product product = repository.findById(productId).orElseThrow(NotFoundException::new);
        for (Product p : products) {
            product.addToExchange(p);
        }
        repository.save(product);
    }


    public void addFromHome(Long productId, List<Product> products) {
        Product product = repository.findById(productId).orElseThrow(NotFoundException::new);
        for (Product p : products) {
            p.addToExchange(product);
        }
        repository.save(product);
    }

    public void deleteExchange(Long availableId, Long exchangeId) {
        Product available = repository.findById(availableId).orElseThrow(NotFoundException::new);
        Product exchange = repository.findById(exchangeId).orElseThrow(NotFoundException::new);
        available.deleteFromExchange(exchange);
        repository.save(available);
    }

    public Product productInfo(Long productId) {
        return repository.findById(productId).orElseThrow(NotFoundException::new);
    }

    public boolean isFaves(Long productId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(NotFoundException::new);
        Product product = repository.findById(productId).orElseThrow(NotFoundException::new);
        return user.getFavorites().contains(product);
    }

    public List<Product> getByName(String name) {
        return repository.findByName(name);
    }

    public List<List<Product>> getPaths() {
        List<List<Product>> listCycles = new ArrayList<>();


        SearchPath s = new SearchPath(repository.findAll());
        s.buildMatrixHash();

        List<List<Vertex>> allCycles = s.getAllCycles();

        List<Vertex> alreadyInclude = new ArrayList<>();
        List<List<Vertex>> finalCycles = new ArrayList<>();

        while (chooseCycle(allCycles, alreadyInclude, finalCycles)) {}

        for (List<Vertex> cycles: finalCycles) {
            List<Product> products = new ArrayList<>();
            for (Vertex vertex: cycles) {
                products.add(vertex.getProduct());
            }
            listCycles.add(products);
        }

        return listCycles;
    }

    private static boolean chooseCycle(List<List<Vertex>> allCycles, List<Vertex> alreadyInclude, List<List<Vertex>> finalCycles) {
        int max = 0;
        int imax = 0;
        for (List<Vertex> cycle: allCycles) {
            if (cycle.size() > max && !isContain(cycle, alreadyInclude)) {
                max = cycle.size();
                imax = allCycles.indexOf(cycle);
            }
        }
        if (max != 0) {
            alreadyInclude.addAll(allCycles.get(imax));
            finalCycles.add(allCycles.get(imax));
            allCycles.remove(allCycles.get(imax));
            return true;
        } else {
            return false;
        }
    }

    private static boolean isContain(List<Vertex> a, List<Vertex> b) {
        for (Vertex i: b) {
            if (a.contains(i)) return true;
        }
        return false;
    }
}
